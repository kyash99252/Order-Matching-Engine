package com.example.exchange.domain;

/**
 * Represents the side of an order: BUY or SELL
 * This enum is a core part of domain model, preventing "stringly-typed" code
 */
public enum Side {
    BUY,
    SELL
}