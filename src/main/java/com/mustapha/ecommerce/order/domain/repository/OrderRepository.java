package com.mustapha.ecommerce.order.domain.repository;

import java.util.List;
import java.util.Optional;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Repository Interface - Domain Layer
 * Responsibility: Define contract for order persistence (Port in Hexagonal Architecture)
 * Pattern: Repository (abstraction)
 * SOLID: DIP (interface in domain, implementation in infrastructure)
 * 
 * Domain Rules:
 * - Uses OrderId value object (not String) for type safety
 * - Returns Optional for single results (null-safe)
 * - All methods use domain objects (Order, OrderId)
 */
public interface OrderRepository {
    /**
     * Save or update an order aggregate
     * After saving, infrastructure should publish domain events
     * @param order the order to save
     * @return the saved order (with updated state if needed)
     */
    Order save(Order order);
    
    /**
     * Find order by its unique identifier
     * @param id the order ID
     * @return Optional containing order if found, empty otherwise
     */
    Optional<Order> findById(OrderId id);
    
    /**
     * Find all orders for a specific customer
     * Note: Consider pagination for production use if customer can have many orders
     * @param customerId the customer identifier
     * @return list of orders (empty list if none found)
     */
    List<Order> findByCustomerId(String customerId);
    
    /**
     * Check if order exists
     * @param id the order ID
     * @return true if order exists, false otherwise
     */
    boolean existsById(OrderId id);
    
    /**
     * Delete an order by its identifier
     * Use with caution - consider soft delete for audit trail
     * @param id the order ID to delete
     */
    void deleteById(OrderId id);
    
    /**
     * Get total count of orders
     * @return total number of orders
     */
    long count();
}
