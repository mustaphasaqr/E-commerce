package com.mustapha.ecommerce.product.domain.model.valueobject;

import java.util.Objects;

/**
 * Stock Value Object
 * Responsibility: Product stock/inventory management
 * Pattern: Value Object (DDD)
 */
public class Stock {
    private final int quantity;

    private Stock(int quantity) {
        this.quantity = quantity;
    }

    public static Stock of(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        return new Stock(quantity);
    }

    public static Stock empty() {
        return new Stock(0);
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    public boolean hasQuantity(int required) {
        return quantity >= required;
    }

    public Stock reserve(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot reserve negative amount");
        }
        if (amount > quantity) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + quantity + ", Requested: " + amount);
        }
        return new Stock(quantity - amount);
    }

    public Stock restock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot restock negative amount");
        }
        return new Stock(quantity + amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return quantity == stock.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity);
    }

    @Override
    public String toString() {
        return String.valueOf(quantity);
    }
}
