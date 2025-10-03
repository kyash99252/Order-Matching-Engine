package com.example.exchange.service;

import com.example.exchange.domain.Order;
import com.example.exchange.domain.Trade;
import com.example.exchange.repository.OrderRepository;
import com.example.exchange.repository.TradeRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersistenceService {

    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;

    public PersistenceService(TradeRepository tradeRepository, OrderRepository orderRepository) {
        this.tradeRepository = tradeRepository;
        this.orderRepository = orderRepository;
    }

    @Async
    @Transactional
    public void persistTradesAndOrders(List<Trade> trades, List<Order> updatedOrders) {
        if (trades != null && !trades.isEmpty()) {
            tradeRepository.saveAll(trades);
        }
        if (updatedOrders != null && !updatedOrders.isEmpty()) {
            orderRepository.saveAll(updatedOrders);
        }
    }
}