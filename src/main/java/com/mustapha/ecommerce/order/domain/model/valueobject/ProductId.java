package com.mustapha.ecommerce.order.domain.model.valueobject;

import java.util.Objects;

/**
 * Product ID Value Object
 * 
 * Responsibility: Type-safe product identifier
 * 
 * Pattern: Value Object
 * - Immutable (final class, final field)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Business Rules:
 * - Product ID cannot be null or blank
 * - Length between 1-100 characters
 * 
 * Benefits:
 * - Prevents mixing product IDs with customer IDs or order IDs (compile-time safety)
 * - Encapsulates validation logic
 * - Clear domain language: ProductId vs String
 */
public final class ProductId {
    
    private final String value;
    
    /**
     * Create a ProductId with validation
     * 
     * @param value The product identifier
     * @throws IllegalArgumentException if value is null, blank, or invalid length
     */
    public ProductId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Product ID cannot exceed 100 characters");
        }
        
        this.value = value;
    }
    
    /**
     * Factory method: Generate a new unique product ID
     * 
     * @return A new ProductId with a generated value
     */
    public static ProductId generate() {
        return new ProductId("PROD-" + java.util.UUID.randomUUID().toString());
    }
    
    public String getValue() {
        return value;
    }
    
    // Value Object equality - by value, not identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId that = (ProductId) o;
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
