package com.mustapha.ecommerce.order.domain.service;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Discount Policy Interface
 * Pattern: Strategy
 * Responsibility: Define contract for discount calculation
 * 
 * Implementations can consider:
 * - Customer loyalty tier
 * - Order total amount
 * - Promotional codes
 * - Seasonal discounts
 */
public interface DiscountPolicy {
    /**
     * Apply discount to base price
     * 
     * @param order the order (may need customer info, items, etc.)
     * @param basePrice the base price before discount
     * @return final price after discount
     */
    Money applyDiscount(Order order, Money basePrice);
}
