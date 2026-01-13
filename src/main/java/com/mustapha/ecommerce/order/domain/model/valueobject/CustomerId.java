package com.mustapha.ecommerce.order.domain.model.valueobject;

import java.util.Objects;

/**
 * Customer ID Value Object
 * 
 * Responsibility: Type-safe customer identifier
 * 
 * Pattern: Value Object
 * - Immutable (final class, final field)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Business Rules:
 * - Customer ID cannot be null or blank
 * - Length between 1-100 characters
 * 
 * Benefits:
 * - Prevents mixing customer IDs with product IDs or order IDs (compile-time safety)
 * - Encapsulates validation logic
 * - Clear domain language: CustomerId vs String
 */
public final class CustomerId {
    
    private final String value;
    
    /**
     * Create a CustomerId with validation
     * 
     * @param value The customer identifier
     * @throws IllegalArgumentException if value is null, blank, or invalid length
     */
    public CustomerId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Customer ID cannot exceed 100 characters");
        }
        
        this.value = value;
    }
    
    /**
     * Factory method: Generate a new unique customer ID
     * 
     * @return A new CustomerId with a generated value
     */
    public static CustomerId generate() {
        return new CustomerId("CUST-" + java.util.UUID.randomUUID().toString());
    }
    
    public String getValue() {
        return value;
    }
    
    // Value Object equality - by value, not identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerId that = (CustomerId) o;
        return Objects.equals(value, that.value);
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
