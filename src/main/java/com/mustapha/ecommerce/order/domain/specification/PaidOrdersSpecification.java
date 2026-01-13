package com.mustapha.ecommerce.order.domain.specification;

import com.mustapha.ecommerce.order.domain.model.Order;

/**
 * Paid Orders Specification
 * 
 * Business Rule: Order is considered "paid" when status is PAID or beyond
 * (PAID, PROCESSING, SHIPPED, DELIVERED)
 * 
 * Usage Example:
 * <pre>
 * OrderRepository repo = ...;
 * List<Order> paidOrders = repo.findBySpecification(
 *     new PaidOrdersSpecification()
 * );
 * </pre>
 * 
 * This is a SIMPLE implementation to demonstrate the pattern.
 * Per reviewer's advice: "واحد بس implement والباقي placeholders"
 */
public class PaidOrdersSpecification implements OrderSpecification {
    
    @Override
    public boolean isSatisfiedBy(Order order) {
        if (order == null) {
            return false;
        }
        
        // Delegate to Order's domain behavior
        return order.isPaid();
    }
    
    @Override
    public String toString() {
        return "PaidOrdersSpecification{}";
    }
}
