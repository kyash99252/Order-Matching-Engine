package com.example.exchange.service;

import com.example.exchange.api.dto.OrderBookDto;
import com.example.exchange.api.dto.OrderRequest;
import com.example.exchange.core.matching.MatchingEngine;
import com.example.exchange.core.orderbook.OrderBook;
import com.example.exchange.domain.Order;
import com.example.exchange.domain.OrderStatus;
import com.example.exchange.domain.Trade;
import com.example.exchange.repository.OrderBookCache;
import com.example.exchange.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * High-performance implementation of OrderService.
 * Design Philosophy: This implementation is designed for speed and responsiveness.
 * The placeNewOrder method does the bare minimum of synchronous work (a single INSERT)
 * and then delegates all heavy I/O operations (saving trades, updating matched orders)
 * to a background thread via the PersistenceService. This ensures the API endpoint
 * returns to the client almost instantly, even under heavy load.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final MatchingEngine matchingEngine;
    private final OrderBookCache orderBookCache;
    private final SimpMessagingTemplate messagingTemplate;
    private final PersistenceService persistenceService; // The async service

    public OrderServiceImpl(OrderRepository orderRepository,
                            MatchingEngine matchingEngine,
                            OrderBookCache orderBookCache,
                            SimpMessagingTemplate messagingTemplate,
                            PersistenceService persistenceService) {
        this.orderRepository = orderRepository;
        this.matchingEngine = matchingEngine;
        this.orderBookCache = orderBookCache;
        this.messagingTemplate = messagingTemplate;
        this.persistenceService = persistenceService;
    }

    @Override
    @Transactional
    public Order placeNewOrder(OrderRequest orderRequest) {
        // 1. Create and save the initial order
        Order order = new Order(
                orderRequest.getSymbol(),
                orderRequest.getSide(),
                orderRequest.getOrderType(),
                orderRequest.getPrice(),
                orderRequest.getQuantity()
        );
        orderRepository.save(order);
        log.info("Persisted initial state of new order: {}", order.getId());

        // 2. Process the order in the IN-MEMORY matching engine
        List<Trade> trades = matchingEngine.processOrder(order);

        // 3. Update the state of the incoming order in memory
        if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.FILLED);
        } else if (order.getRemainingQuantity().compareTo(order.getQuantity()) < 0) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }

        // 4. Gather all entities that need to be persisted in the background
        List<Order> ordersToUpdate = new ArrayList<>();
        ordersToUpdate.add(order); // Always update the incoming order's final status

        if (!trades.isEmpty()) {
            // Get the unique IDs of all resting orders that were matched
            Set<Long> restingOrderIds = trades.stream()
                    .flatMap(trade -> Stream.of(trade.getBuyOrderId(), trade.getSellOrderId()))
                    .filter(orderId -> !orderId.equals(order.getId()))
                    .collect(Collectors.toSet());
            
            // Retrieve the full Order objects for the matched resting orders
            List<Order> restingOrders = orderRepository.findAllById(restingOrderIds);

            // Update their status based on their remaining quantity
            restingOrders.forEach(ro -> {
                if (ro.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    ro.setStatus(OrderStatus.FILLED);
                } else {
                    ro.setStatus(OrderStatus.PARTIALLY_FILLED);
                }
            });
            ordersToUpdate.addAll(restingOrders);

            // Broadcast trades over WebSocket
            trades.forEach(trade -> {
                log.info("Broadcasting trade: {}", trade.getId());
                messagingTemplate.convertAndSend("/topic/trades", trade);
            });
        }
        
        // 5. FIRE AND FORGET: Delegate the slow I/O to the background persistence service.
        // This call returns immediately, it does not wait for the saves to complete.
        persistenceService.persistTradesAndOrders(trades, ordersToUpdate);
        log.info("Delegated persistence of {} trades and {} updated orders to background service.", trades.size(), ordersToUpdate.size());
        
        // 6. Update the cached order book. This is a fast in-memory operation.
        updateCachedOrderBook(order.getSymbol());
        
        // 7. Return the initial state of the order to the client immediately.
        return order;
    }

    // No changes needed below this line
    @Override
    public OrderBookDto getOrderBook(String symbol) {
        return orderBookCache.getOrderBook(symbol).orElseGet(() -> {
            log.warn("Cache miss for order book: {}. Rebuilding from engine state.", symbol);
            OrderBookDto dto = buildOrderBookDto(symbol);
            orderBookCache.updateOrderBook(symbol, dto);
            return dto;
        });
    }

    private void updateCachedOrderBook(String symbol) {
        OrderBookDto dto = buildOrderBookDto(symbol);
        orderBookCache.updateOrderBook(symbol, dto);
        log.info("Updated order book cache for symbol: {}", symbol);
    }

    private OrderBookDto buildOrderBookDto(String symbol) {
        OrderBook book = matchingEngine.getOrderBook(symbol);
        if (book == null) {
            return new OrderBookDto(symbol, List.of(), List.of());
        }

        List<OrderBookDto.PriceLevel> bids = book.getBids().entrySet().stream()
                .map(entry -> new OrderBookDto.PriceLevel(
                        entry.getKey(),
                        entry.getValue().stream().map(Order::getRemainingQuantity).reduce(BigDecimal.ZERO, BigDecimal::add)
                )).collect(Collectors.toList());
        
        List<OrderBookDto.PriceLevel> asks = book.getAsks().entrySet().stream()
                .map(entry -> new OrderBookDto.PriceLevel(
                        entry.getKey(),
                        entry.getValue().stream().map(Order::getRemainingQuantity).reduce(BigDecimal.ZERO, BigDecimal::add)
                )).collect(Collectors.toList());

        return new OrderBookDto(symbol, bids, asks);
    }
}