package com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject;

import java.util.Objects;

/**
 * Order ID Value Object
 * Responsibility: Immutable representation of order identifier
 * Pattern: Value Object
 */
public final class OrderId {
    private final String value;

    public OrderId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        this.value = value;
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
