package com.example.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exchange.domain.Order;

/**
 * Spring Data JPA repository for the Order entity.
 * Design Principle (Interface Segregation): This interface provides a clean, specific contract for
 * order persistence operations without exposing underlying implementation details (like Hibernate or JDBC).
 * Services will depend on this abstraction, not a concrete class.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Spring Data JPA automatically provides implementations for common methods like save(), findById(), findAll(), etc.
}
