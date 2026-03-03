package com.mustapha.ecommerce.cart.domain.model.valueobject;

import java.util.Objects;

/**
 * Cart ID Value Object
 * 
 * Responsibility: Type-safe cart identifier
 * 
 * Pattern: Value Object
 * - Immutable (final class, final field)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Business Rules:
 * - Cart ID must be positive
 * - Cannot be null
 * 
 * Benefits:
 * - Prevents mixing cart IDs with product IDs or user IDs (compile-time safety)
 * - Encapsulates validation logic
 * - Clear domain language: CartId vs Long
 */
public final class CartId {
    
    private final Long value;
    
    /**
     * Create a CartId with validation
     * 
     * @param value The cart identifier
     * @throws IllegalArgumentException if value is null or not positive
     */
    public CartId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Cart ID cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Cart ID must be positive, got: " + value);
        }
        
        this.value = value;
    }
    
    /**
     * Get the raw value (for persistence layer)
     */
    public Long getValue() {
        return value;
    }
    
    // ========== Value Object Equality ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartId cartId = (CartId) o;
        return Objects.equals(value, cartId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "CartId(" + value + ")";
    }
}
