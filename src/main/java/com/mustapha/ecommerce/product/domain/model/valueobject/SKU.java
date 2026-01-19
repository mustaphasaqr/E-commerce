package com.mustapha.ecommerce.product.domain.model.valueobject;

import java.util.Objects;

/**
 * SKU (Stock Keeping Unit) Value Object
 * Responsibility: Unique product identifier for inventory
 * Pattern: Value Object (DDD)
 */
public class SKU {
    private final String value;

    private SKU(String value) {
        this.value = value;
    }

    public static SKU of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        
        String normalized = value.toUpperCase().trim();
        
        // Business Rule: SKU must be 6-30 characters, uppercase letters, numbers, and hyphens only
        if (!normalized.matches("^[A-Z0-9\\-]{6,30}$")) {
            throw new IllegalArgumentException(
                "SKU must be 6-30 characters and contain only uppercase letters, numbers, and hyphens. Got: " + value
            );
        }
        
        return new SKU(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SKU sku = (SKU) o;
        return Objects.equals(value, sku.value);
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
