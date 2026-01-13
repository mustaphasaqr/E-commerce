package com.mustapha.ecommerce.order.domain.specification;

import com.mustapha.ecommerce.order.domain.model.Order;

/**
 * Order Specification Interface
 * 
 * Pattern: Specification Pattern
 * Purpose: Encapsulate business rules for querying/filtering orders
 * 
 * Benefits:
 * - Single Responsibility: Each specification has ONE query rule
 * - Open/Closed: Add new queries without changing repository
 * - Testable: Test specifications independently
 * - Reusable: Combine specifications (AND, OR, NOT)
 * - Domain Language: Queries expressed in business terms
 * 
 * Usage Example:
 * <pre>
 * OrderRepository repo = ...;
 * 
 * // Simple query
 * List<Order> paidOrders = repo.findBySpecification(
 *     new PaidOrdersSpecification()
 * );
 * 
 * // Combined query (future)
 * List<Order> paidCustomerOrders = repo.findBySpecification(
 *     new PaidOrdersSpecification().and(
 *         new CustomerOrdersSpecification("customer-123")
 *     )
 * );
 * </pre>
 * 
 * When to Use:
 * ✅ Complex filtering logic
 * ✅ Need to combine multiple criteria dynamically
 * ✅ Business rules for querying are complex
 * 
 * When NOT to Use:
 * ❌ Simple queries (findById, findAll)
 * ❌ Static queries that never change
 * 
 * NOTE: Per reviewer's advice - "interface + 1 simple implementation only for now"
 */
public interface OrderSpecification {
    
    /**
     * Check if an order satisfies this specification
     * 
     * @param order The order to check
     * @return true if order satisfies the specification, false otherwise
     */
    boolean isSatisfiedBy(Order order);
    
    // Future: Add combinators (AND, OR, NOT) when needed
    // default OrderSpecification and(OrderSpecification other) { ... }
    // default OrderSpecification or(OrderSpecification other) { ... }
    // default OrderSpecification not() { ... }
}
