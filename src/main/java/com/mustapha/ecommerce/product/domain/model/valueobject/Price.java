package com.mustapha.ecommerce.product.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Price Value Object
 * Responsibility: Product pricing with validation
 * Pattern: Value Object (DDD)
 */
public class Price {
    private final BigDecimal amount;

    private Price(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Price of(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        return new Price(BigDecimal.valueOf(amount));
    }

    public static Price of(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        return new Price(amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public double getAmountAsDouble() {
        return amount.doubleValue();
    }

    public boolean isGreaterThan(Price other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Price other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return amount.compareTo(price.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
