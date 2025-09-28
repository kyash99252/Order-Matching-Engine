package com.example.exchange.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exchange.api.dto.OrderBookDto;
import com.example.exchange.api.dto.OrderRequest;
import com.example.exchange.domain.Order;
import com.example.exchange.service.OrderService;

import jakarta.validation.Valid;

/**
 * REST controller for handling order-related requests.
 * Design Philosophy (Thin Controller): This controller's responsibility is limited to:
 * 1. Exposing endpoints.
 * 2. Validating incoming DTOs (@Valid).
 * 3. Delegating business logic to the OrderService.
 * 4. Formatting the response.
 * It contains no business logic itself, adhering to the principle of Separation of Concerns.
 */
@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Order newOrder = orderService.placeNewOrder(orderRequest);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    @GetMapping("/orderbook/{symbolPair}")
    public ResponseEntity<OrderBookDto> getOrderBook(@PathVariable String symbolPair) {
        String symbol = symbolPair.replace("_", "/").replace("-", "/").toUpperCase();
        OrderBookDto orderBook = orderService.getOrderBook(symbol);
        return ResponseEntity.ok(orderBook);
    }
}
