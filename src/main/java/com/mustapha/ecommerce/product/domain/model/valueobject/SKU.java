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
        if (!value.matches("^[A-Z0-9\\-]+$")) {
            throw new IllegalArgumentException("SKU must contain only uppercase letters, numbers, and hyphens");
        }
        return new SKU(value.toUpperCase());
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
