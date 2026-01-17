package com.mustapha.ecommerce.order.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Builder
 * Pattern: Builder
 * Responsibility: Construct valid Order aggregates
 * 
 * Refactored to:
 * - Accept CustomerId value object (DDD alignment)
 * - Support incremental item addition via addItem()
 * - Total automatically calculated from items (invariant preserved)
 * 
 * Usage:
 * new OrderBuilder()
 *     .withCustomerId(customerIdValueObject)
 *     .addItem(orderItem1)
 *     .addItem(orderItem2)
 *     .build()
 */
public class OrderBuilder {
    private CustomerId customerId;
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Set customer ID using value object
     * 
     * @param customerId CustomerId value object
     * @return this builder for chaining
     */
    public OrderBuilder withCustomerId(CustomerId customerId) {
        this.customerId = customerId;
        return this;
    }

    /**
     * Add a single item to the order
     * Allows incremental item addition
     * 
     * @param item OrderItem to add
     * @return this builder for chaining
     */
    public OrderBuilder addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }
        this.items.add(item);
        return this;
    }

    /**
     * Build the Order aggregate
     * Validates required fields and creates order in PENDING state
     * Uses Order.addItem() to ensure all domain validations are applied
     * 
     * @return Order in PENDING state with generated ID
     */
    public Order build() {
        validateBuilder();
        
        // Create order using package-private constructor
        Order order = new Order();
        order.setId(OrderId.generate());
        
        // Set customer ID (package-private setter)
        order.setCustomerId(customerId);
        
        // Add items using domain method (applies MAX_TOTAL_QUANTITY validation)
        // This ensures all domain invariants are enforced
        for (OrderItem item : items) {
            order.addItem(item);
        }
        
        return order;
    }

    private void validateBuilder() {
        if (customerId == null) {
            throw new IllegalStateException("Customer ID is required");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Order must have at least one item");
        }
    }
}
