package com.example.exchange.api.dto;

import java.math.BigDecimal;

import com.example.exchange.domain.OrderType;
import com.example.exchange.domain.Side;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for receiving new order requests.
 * Uses @Valid annotations for fail-fast input validation, a tenet of defensive programming.
 */
@Data
public class OrderRequest {
    
    @NotBlank(message = "Symbol cannot be blank")
    private String symbol;

    @NotNull(message = "Side is required")
    private Side side;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Price is required for LIMIT orders")
    @DecimalMin(value = "0.00000001", message = "Price must be positive")
    private BigDecimal price;
}
