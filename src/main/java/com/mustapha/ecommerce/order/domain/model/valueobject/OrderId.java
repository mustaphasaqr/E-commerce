package com.mustapha.ecommerce.order.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Order ID Value Object
 * Responsibility: Immutable representation of order identifier
 * Pattern: Value Object
 * Domain Rules:
 * - Cannot be null or empty
 * - Must be valid UUID format OR alphanumeric with min length
 * - Immutable once created
 */
public final class OrderId {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;
    
    private final String value;

    public OrderId(String value) {
        validateOrderId(value);
        this.value = value;
    }
    
    // ========== Domain Rules ==========
    
    private void validateOrderId(String value) {
        // Rule: Cannot be null or empty
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        // Rule: Length constraints
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Order ID length must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
            );
        }
        
        // Optional: Validate format (uncomment if you want strict UUID validation)
        // try {
        //     UUID.fromString(value);
        // } catch (IllegalArgumentException e) {
        //     throw new IllegalArgumentException("Order ID must be a valid UUID format", e);
        // }
    }
    
    /**
     * Factory method: Generate new Order ID using UUID
     */
    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
