package com.example.exchange.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DDD Entity: Represents a completed trade that occurred from a match.
 * Trades are the immutable results of the matching process and are persisted for auditing and record-keeping.
 */
@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Long buyOrderId;

    @Column(nullable = false)
    private Long sellOrderId;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    public Trade(String symbol, Long buyOrderId, Long sellOrderId, BigDecimal price, BigDecimal quantity) {
        this.symbol = symbol;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }
}
