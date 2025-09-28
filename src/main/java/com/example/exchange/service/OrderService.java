package com.example.exchange.service;

import com.example.exchange.api.dto.OrderBookDto;
import com.example.exchange.api.dto.OrderRequest;
import com.example.exchange.domain.Order;

/**
 * The abstraction for the business logic layer.
 * Design Philosophy: Controllers depend on this interface, not the implementation.
 * This allows for flexibility (e.g., creating a different implementation) and easy mocking in tests.
 */
public interface OrderService {
     /**
     * Places a new order and processes it through the matching engine.
     *
     * @param orderRequest DTO containing the new order details.
     * @return The persisted Order entity.
     */
    Order placeNewOrder(OrderRequest orderRequest);

    /**
     * Retrieves the current state of the order book for a given symbol.
     *
     * @param symbol The trading symbol (e.g., "BTC/USD").
     * @return A DTO representing the order book.
     */
    OrderBookDto getOrderBook(String symbol);
}
