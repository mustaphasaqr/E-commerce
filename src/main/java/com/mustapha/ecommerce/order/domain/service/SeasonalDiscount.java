package com.mustapha.ecommerce.order.domain.service;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Seasonal Discount Policy
 * Pattern: Strategy (implementation)
 * Responsibility: Apply percentage-based discount
 * 
 * Domain Rules:
 * - Discount rate must be between 0 and 1 (0% to 100%)
 * - Final price cannot be negative
 * - Order and basePrice must not be null
 */
public class SeasonalDiscount implements DiscountPolicy {

    private static final double DEFAULT_DISCOUNT_RATE = 0.10; // 10% discount
    
    private final double discountRate;

    /**
     * Constructor with default 10% discount
     */
    public SeasonalDiscount() {
        this(DEFAULT_DISCOUNT_RATE);
    }
    
    /**
     * Constructor with custom discount rate
     * @param discountRate percentage as decimal (e.g., 0.10 for 10%)
     */
    public SeasonalDiscount(double discountRate) {
        validateDiscountRate(discountRate);
        this.discountRate = discountRate;
    }
    
    private void validateDiscountRate(double rate) {
        if (rate < 0 || rate > 1) {
            throw new IllegalArgumentException(
                "Discount rate must be between 0 and 1, got: " + rate
            );
        }
    }

    @Override
    public Money applyDiscount(Order order, Money basePrice) {
        // Validation
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (basePrice == null) {
            throw new IllegalArgumentException("Base price cannot be null");
        }
        
        // Calculate discount amount
        Money discountAmount = basePrice.multiply(discountRate);
        
        // Apply discount
        Money finalPrice = basePrice.subtract(discountAmount);
        
        return finalPrice;
    }
    
    public double getDiscountRate() {
        return discountRate;
    }
}
