package com.mustapha.ecommerce.cart.domain.model.valueobject;

import java.util.Objects;

/**
 * User ID Value Object
 * 
 * Responsibility: Type-safe user/customer identifier
 * 
 * Pattern: Value Object
 * - Immutable (final class, final field)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Business Rules:
 * - User ID must be positive
 * - Cannot be null
 * 
 * Benefits:
 * - Prevents mixing user IDs with cart IDs or product IDs (compile-time safety)
 * - Encapsulates validation logic
 * - Clear domain language: UserId vs Long
 */
public final class UserId {
    
    private final Long value;
    
    /**
     * Create a UserId with validation
     * 
     * @param value The user identifier
     * @throws IllegalArgumentException if value is null or not positive
     */
    public UserId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("User ID must be positive, got: " + value);
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
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "UserId(" + value + ")";
    }
}
