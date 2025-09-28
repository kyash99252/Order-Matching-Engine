package com.example.exchange.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exchange.api.dto.OrderBookDto;
import com.example.exchange.api.dto.OrderRequest;
import com.example.exchange.core.matching.MatchingEngine;
import com.example.exchange.core.orderbook.OrderBook;
import com.example.exchange.domain.Order;
import com.example.exchange.domain.OrderStatus;
import com.example.exchange.domain.Trade;
import com.example.exchange.repository.OrderBookCache;
import com.example.exchange.repository.OrderRepository;
import com.example.exchange.repository.TradeRepository;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final MatchingEngine matchingEngine;
    private final OrderBookCache orderBookCache;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderServiceImpl(OrderRepository orderRepository, TradeRepository tradeRepository, MatchingEngine matchingEngine, OrderBookCache orderBookCache, SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.matchingEngine = matchingEngine;
        this.orderBookCache = orderBookCache;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public Order placeNewOrder(OrderRequest orderRequest) {
        // 1. Create and save the initial order
        // It's saved first to get a database ID, which is crucial for trade record
        Order order = new Order(orderRequest.getSymbol(), orderRequest.getSide(), orderRequest.getOrderType(), orderRequest.getPrice(), orderRequest.getQuantity());
        orderRepository.save(order);
        log.info("Persisted new order: {}", order);

        // 2. Process the order through the matching engine
        List<Trade> trades = matchingEngine.processOrder(order);

        // 3. If trades occurred, process them.
        if (!trades.isEmpty()) {
            // Persist all generated trades
            tradeRepository.saveAll(trades);
            log.info("Persisted {} trades", trades.size());

            // Update the status of the orders involved in the trades
            updateOrderStatusFromTrades(trades);

            // Broadcast trades over WebSocket
            trades.forEach(trade -> {
                log.info("Broadcasting trade: {}", trade);
                messagingTemplate.convertAndSend("/topic/trades", trade);
            });
        }

        // 4. Update the order's own status based on its remaining quantity.
        if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.FILLED);
        } else if (order.getRemainingQuantity().compareTo(order.getQuantity()) < 0) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
        orderRepository.save(order);
        
        // 5. Update the cached order book
        updateCachedOrderBook(order.getSymbol());
        return order;
    }

    @Override
    public OrderBookDto getOrderBook(String symbol) {
        // First, try to get it from the cache
        return orderBookCache.getOrderBook(symbol).orElseGet(() -> {
            // If not in cache, build it from the engine's state and cache it
            log.warn("Cache miss for order book: {}. Rebuilding from engine state.", symbol);

            OrderBookDto dto = buildOrderBookDto(symbol);
            orderBookCache.updateOrderBook(symbol, dto);
            return dto;
        });
    }

    private void updateOrderStatusFromTrades(List<Trade> trades) {
        List<Long> updateOrderIds = new ArrayList<>();
        trades.forEach(trade -> {
            updateOrderIds.add(trade.getBuyOrderId());
            updateOrderIds.add(trade.getSellOrderId());
        });

        // Fetch all affected orders in a single query for efficiency
        orderRepository.findAllById(updateOrderIds.stream().distinct().toList()).forEach(o -> {
            if (o.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
                o.setStatus(OrderStatus.FILLED);
            } else {
                o.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
            orderRepository.save(o);
        });
    }

    private void updateCachedOrderBook(String symbol) {
        OrderBookDto dto = buildOrderBookDto(symbol);
        orderBookCache.updateOrderBook(symbol, dto);
        log.info("Updated order book cache for symbol: {}", symbol);
    }

    private OrderBookDto buildOrderBookDto(String symbol) {
        OrderBook book = matchingEngine. getOrderBook(symbol);
        if (book == null) {
            // Return an empty order book if the symbol has no activity yet
            return new OrderBookDto(symbol, List.of(), List.of());
        }

        // Convert the detailed order book into a summarized DTO for public view
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
