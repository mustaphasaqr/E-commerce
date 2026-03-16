package com.mustapha.ecommerce.cart.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Product ID Value Object (Cart Context)
 * 
 * Responsibility: Type-safe product identifier within cart context
 * 
 * Pattern: Value Object
 * - Immutable (final class, final field)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Business Rules:
 * - Product ID must be positive
 * - Cannot be null
 * 
 * Benefits:
 * - Prevents mixing product IDs with cart IDs or user IDs (compile-time safety)
 * - Encapsulates validation logic
 * - Clear domain language: ProductId vs Long
 * 
 * Note: Each bounded context (cart, order, product) has its own ProductId value object
 * to maintain bounded context isolation in DDD
 */
public final class ProductId {
    
    private final String value;
    
    /**
     * Create a ProductId with validation
     * 
     * @param value The product identifier
     * @throws IllegalArgumentException if value is null or not positive
     */
    public ProductId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Product ID must be a valid UUID format. Got: " + value, ex);
        }
        
        this.value = value;
    }
    
    /**
     * Get the raw value (for persistence layer)
     */
    public String getValue() {
        return value;
    }
    
    // ========== Value Object Equality ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return Objects.equals(value, productId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "ProductId(" + value + ")";
    }
}
