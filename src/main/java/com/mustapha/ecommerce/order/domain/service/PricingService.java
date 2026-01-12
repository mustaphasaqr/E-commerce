package com.mustapha.ecommerce.order.domain.service;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Pricing Service - Domain Service
 * Responsibility: Calculate final price with discounts (cross-aggregate logic)
 * Pattern: Domain Service (pure domain, no framework dependencies!)
 * 
 * Domain Rules:
 * - Order cannot be null
 * - Discount policy must be provided
 * - Final price cannot be negative
 */
public class PricingService {

    private final DiscountPolicy discountPolicy;

    public PricingService(DiscountPolicy discountPolicy) {
        if (discountPolicy == null) {
            throw new IllegalArgumentException("Discount policy cannot be null");
        }
        this.discountPolicy = discountPolicy;
    }

    /**
     * Calculate final price for an order
     * Applies discount policy to base price
     * 
     * @param order the order to price
     * @return final price after discounts
     * @throws IllegalArgumentException if order is null
     */
    public Money calculateFinalPrice(Order order) {
        // Validation
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        // Get base price from order items
        Money basePrice = order.getTotalAmount();
        
        // Apply discount policy to get final price
        Money discountedPrice = discountPolicy.applyDiscount(order, basePrice);
        
        // Domain rule: Final price cannot be negative
        if (discountedPrice.isLessThan(new Money(0))) {
            throw new IllegalStateException("Final price cannot be negative");
        }
        
        return discountedPrice;
    }
}
