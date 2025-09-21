package com.example.exchange.repository;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.exchange.api.dto.OrderBookDto;

/**
 * Manages the caching of order books in Redis.
 * Design Principle (High Cohesion): This class is solely responsible for Redis interactions
 * related to order books. It encapsulates key generation, serialization, and TTL management.
 * This decouples the core application logic from the caching mechanism.
 */
@Repository
public class OrderBookCache {
    
    private static final String KEY_PREFIX = "orderbook:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final RedisTemplate<String, OrderBookDto> redisTemplate;

    public OrderBookCache(RedisTemplate<String, OrderBookDto> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Retrieves an order book from the Redis cache.
     *
     * @param symbol The trading symbol (e.g., "BTC/USD").
     * @return An Optional containing the OrderBookDto if found, otherwise an empty Optional.
     */
    public Optional<OrderBookDto> getOrderBook(String symbol) {
        OrderBookDto orderBook = redisTemplate.opsForValue().get(getKey(symbol));
        return Optional.ofNullable(orderBook);
    }

    /**
     * Updates or creates an order book in the Redis cache.
     *
     * @param symbol The trading symbol.
     * @param orderBook The order book data to cache.
     */
    public void updateOrderBook(String symbol, OrderBookDto orderBook) {
        redisTemplate.opsForValue().set(getKey(symbol), orderBook, CACHE_TTL);
    }

    /**
     * Generates a standardized key for storing order books in Redis.
     */
    private String getKey(String symbol) {
        return KEY_PREFIX + symbol.replace("/", "_").toUpperCase();
    }
}
