package com.mustapha.ecommerce.order.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money Value Object
 * Responsibility: Immutable representation of monetary value
 * Pattern: Value Object
 * Domain Rules:
 * - Amount cannot be negative
 * - Uses BigDecimal for precision (not double!)
 * - Immutable - all operations return new Money instance
 * - Scale fixed at 2 decimal places
 */
public final class Money {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private final BigDecimal amount;

    public Money(double amount) {
        this(BigDecimal.valueOf(amount));
    }
    
    public Money(BigDecimal amount) {
        validateAmount(amount);
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }
    
    // ========== Domain Rules ==========
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
    }
    
    // ========== Domain Operations ==========

    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot add null Money");
        }
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot subtract null Money");
        }
        
        BigDecimal result = this.amount.subtract(other.amount);
        
        // Rule: Cannot subtract more than you have
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Cannot subtract " + other + " from " + this + " - result would be negative"
            );
        }
        
        return new Money(result);
    }

    public Money multiply(double factor) {
        // Rule: Factor cannot be negative
        if (factor < 0) {
            throw new IllegalArgumentException("Cannot multiply money by negative factor: " + factor);
        }
        
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
    }
    
    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot multiply money by negative quantity: " + quantity);
        }
        
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }
    
    // ========== Comparison Methods ==========
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    public boolean isLessThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) <= 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public double getAmount() {
        return amount.doubleValue();
    }
    
    public BigDecimal getAmountAsBigDecimal() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0; // Use compareTo for BigDecimal
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros()); // Normalize for hashing
    }

    @Override
    public String toString() {
        return amount.setScale(SCALE, ROUNDING_MODE).toPlainString();
    }
}
