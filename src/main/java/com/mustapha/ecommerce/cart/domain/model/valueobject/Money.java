package com.mustapha.ecommerce.cart.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money Value Object (Cart Context)
 * 
 * Responsibility: Immutable representation of monetary value
 * 
 * Pattern: Value Object
 * - Immutable (final class, final field)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Domain Rules:
 * - Amount cannot be negative
 * - Uses BigDecimal for precision (not double!)
 * - Immutable - all operations return new Money instance
 * - Scale fixed at 2 decimal places (cents)
 * 
 * Benefits:
 * - Prevents arithmetic errors with floating point
 * - Encapsulates rounding rules
 * - Domain operations (add, multiply) return new Money
 */
public final class Money {
    
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    
    private final BigDecimal amount;
    
    /**
     * Create Money from double (convenience constructor)
     */
    public Money(double amount) {
        this(BigDecimal.valueOf(amount));
    }
    
    /**
     * Create Money from BigDecimal with validation
     */
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
    
    /**
     * Add two amounts
     */
    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot add null Money");
        }
        return new Money(this.amount.add(other.amount));
    }
    
    /**
     * Subtract amount (result cannot be negative)
     */
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
    
    /**
     * Multiply by quantity
     */
    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot multiply money by negative quantity: " + quantity);
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }
    
    /**
     * Compare amounts
     */
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Get the raw value (for persistence layer)
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    // ========== Value Object Equality ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
    
    @Override
    public String toString() {
        return "$" + amount.toPlainString();
    }
}
