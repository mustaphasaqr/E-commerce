package com.mustapha.ecommerce.product.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Product ID Value Object
 * Responsibility: Product identity
 * Pattern: Value Object (DDD)
 */
public class ProductId {
    private final String value;

    private ProductId(String value) {
        this.value = value;
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public static ProductId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        return new ProductId(value);
    }

    public String getValue() {
        return value;
    }

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
        return value;
    }
}
