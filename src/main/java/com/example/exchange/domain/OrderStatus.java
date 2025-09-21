package com.example.exchange.domain;

/**
 * Represents the status of an order throughout its lifecycle
 */
public enum OrderStatus {
    OPEN,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED
}
