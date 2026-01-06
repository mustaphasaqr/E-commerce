package com.mustapha.ecommerce.ecommerce.order.domain.repository;

import java.util.List;
import java.util.Optional;

import com.mustapha.ecommerce.ecommerce.order.domain.model.Order;

/**
 * Order Repository Interface - Domain Layer
 * Responsibility: Define contract for order persistence
 * Pattern: Repository (abstraction)
 * SOLID: DIP (interface in domain, implementation in infrastructure)
 */
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    List<Order> findByCustomerId(String customerId);
    void delete(String id);
}
