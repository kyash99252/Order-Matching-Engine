package com.example.exchange.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DDD Entity: Represents an order in the system.
 * This is a central entity, persisted in the database.
 * It's designed to be rich with information about a client's intent to trade.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal remainingQuantity;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    public Order(String symbol, Side side, OrderType orderType, BigDecimal price, BigDecimal quantity) {
        this.symbol = symbol;
        this.side = side;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.OPEN;
        this.timestamp = Instant.now();
    }
}
