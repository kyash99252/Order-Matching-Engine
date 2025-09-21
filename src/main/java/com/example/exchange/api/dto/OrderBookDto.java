package com.example.exchange.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing the public view of an order book.
 * This is what clients will see when they query the order book state.
 * Implements Serializable for Redis caching.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookDto implements Serializable {
    
    private String symbol;
    private List<PriceLevel> bids;
    private List<PriceLevel> asks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceLevel implements Serializable {
        private BigDecimal price;
        private BigDecimal totalQuantity;
    }
}
