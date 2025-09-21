package com.example.exchange.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
