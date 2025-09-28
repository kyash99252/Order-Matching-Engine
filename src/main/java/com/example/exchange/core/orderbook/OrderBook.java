package com.example.exchange.core.orderbook;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import com.example.exchange.domain.Order;
import com.example.exchange.domain.Side;

/**
 * Represents the order book for a single financial instrument (e.g., BTC/USD).
 * Design Principle (High Cohesion): This class is entirely focused on managing the state of the order book.
 * It uses efficient data structures for its specific purpose:
 * - A TreeMap is used for price levels to keep them sorted by price (desc for bids, asc for asks). This is crucial for matching.
 * - A Deque (FIFO queue) is used for orders at the same price level to maintain time priority.
 */
public class OrderBook {
    
    private final String symbol;

    // Bids (Buy orders): Sorted from highest price to lowest
    private final NavigableMap<BigDecimal, Deque<Order>> bids = new TreeMap<>(Collections.reverseOrder());

    // Asks (Sell orders): Sorted from lowest price to highest
    private final NavigableMap<BigDecimal, Deque<Order>> asks = new TreeMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Removes an order from the book. This would be used for cancellations.
     * @param order The order to remove.
     */
    public void addOrder(Order order) {
        NavigableMap<BigDecimal, Deque<Order>> sideMap = getSideMap(order.getSide());
        sideMap.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
    }

    /**
     * Removes an order from the book. This would be used for cancellations.
     * @param order The order to remove.
     */
    public void removeOrder(Order order) {
        NavigableMap<BigDecimal, Deque<Order>> sideMap = getSideMap(order.getSide());
        Deque<Order> ordersAtPrice = sideMap.get(order.getPrice());
        if (ordersAtPrice != null) {
            ordersAtPrice.removeIf(o -> o.getId().equals(order.getId()));
            if (ordersAtPrice.isEmpty()) {
                sideMap.remove(order.getPrice());
            }
        }
    }

    /**
     * Returns the best bid (highest price).
     * @return An Optional containing the best bid price, or empty if no bids.
     */
    public Optional<BigDecimal> getBestBidPrice() {
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.firstKey());
    }

    /**
     * Returns the best ask (lowest price).
     * @return An Optional containing the best ask price, or empty if no asks.
     */
    public Optional<BigDecimal> getBestAskPrice() {
        return asks.isEmpty() ? Optional.empty() : Optional.of(asks.firstKey());
    }

    /**
     * Gets the queue of orders at the best bid price.
     */
    public Deque<Order> getBestBidOrders() {
        if (bids.isEmpty()) return new LinkedList<>();
        return bids.firstEntry().getValue();
    }

    /**
     * Gets the queue of orders at the best ask price.
     */
    public Deque<Order> getBestAskOrders() {
        if (asks.isEmpty()) return new LinkedList<>();
        return asks.firstEntry().getValue();
    }

    /**
     * A helper method to get the correct map (bids or asks) based on the order side.
     */
    public NavigableMap<BigDecimal, Deque<Order>> getSideMap(Side side) {
        return side == Side.BUY ? bids : asks;
    }

    public String getSymbol() {
        return symbol;
    }

    public NavigableMap<BigDecimal, Deque<Order>> getBids() {
        return bids;
    }

    public NavigableMap<BigDecimal, Deque<Order>> getAsks() {
        return asks;
    }
}
