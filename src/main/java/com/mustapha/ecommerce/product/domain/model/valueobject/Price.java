package com.mustapha.ecommerce.product.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Price Value Object with Currency
 * Responsibility: Product pricing with currency and validation
 * Pattern: Value Object (DDD)
 * 
 * Invariants:
 * - Price cannot be negative
 * - Price must have currency
 * - Currency is immutable
 * - Minimum price is 0.01 (prevents free products by mistake)
 * - Scale is always 2 decimal places (standard monetary precision)
 * 
 * Design Notes:
 * - MINIMUM_PRICE policy is business decision for e-commerce MVP
 * - For free samples/promotions, handle at application layer (discounts, coupons)
 * - BigDecimal preferred over double to avoid floating-point precision errors
 */
public class Price {
    private static final BigDecimal MINIMUM_PRICE = new BigDecimal("0.01");
    
    private final BigDecimal amount;
    private final Currency currency;

    private Price(BigDecimal amount, Currency currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    /**
     * Create price from double value
     * 
     * @deprecated Use {@link #of(BigDecimal, String)} instead.
     * Floating-point arithmetic is imprecise for monetary values.
     * Example: 0.1 + 0.2 = 0.30000000000000004
     */
    @Deprecated
    public static Price of(double amount, String currencyCode) {
        return of(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }

    public static Price of(BigDecimal amount, String currencyCode) {
        return of(amount, Currency.getInstance(currencyCode));
    }

    public static Price of(BigDecimal amount, Currency currency) {
        // Guard: Price cannot be negative
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        
        // Guard: Price must meet minimum (prevent $0.00 mistakes)
        if (amount.compareTo(MINIMUM_PRICE) < 0) {
            throw new IllegalArgumentException(
                "Price must be at least " + MINIMUM_PRICE + " to prevent free products"
            );
        }
        
        // Guard: Currency cannot be null
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        
        return new Price(amount, currency);
    }

    /**
     * Check if this price has same currency as another
     * Business Rule: Cannot compare prices with different currencies
     */
    public void ensureSameCurrency(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot compare prices with different currencies: " + 
                this.currency.getCurrencyCode() + " vs " + other.currency.getCurrencyCode()
            );
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public double getAmountAsDouble() {
        return amount.doubleValue();
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    public boolean isGreaterThan(Price other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Price other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Calculate percentage increase/decrease from this price to another
     * Returns positive value for increase, negative for decrease
     * Example: from $10 to $20 returns 1.0 (100% increase)
     *          from $20 to $10 returns -0.5 (50% decrease)
     * 
     * Used for price change validation (10x max increase, 90% max decrease)
     */
    public BigDecimal calculatePercentageChange(Price newPrice) {
        ensureSameCurrency(newPrice);
        
        if (this.amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Cannot calculate percentage change from zero price");
        }
        
        // Formula: (new - old) / old
        BigDecimal difference = newPrice.amount.subtract(this.amount);
        return difference.divide(this.amount, 4, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return amount.compareTo(price.amount) == 0 && 
               currency.equals(price.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toString() + " " + currency.getCurrencyCode();
    }
}
