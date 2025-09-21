package com.example.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exchange.domain.Trade;

/**
 * Spring Data JPA repository for the Trade entity.
 * This handles the persistence of all matched trades, creating an immutable audit log.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    // Standard CRUD operations are inherited. No custom methods are required for the initial scope.
}
