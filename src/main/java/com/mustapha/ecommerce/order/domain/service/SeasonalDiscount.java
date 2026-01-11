package com.mustapha.ecommerce.order.domain.service;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Seasonal Discount Policy
 * Pattern: Strategy
 */
@Component
public class SeasonalDiscount implements DiscountPolicy {

    private static final double DISCOUNT_RATE = 0.10; // 10% discount

    @Override
    public Money applyDiscount(Order order) {
        Money total = order.getTotalAmount();
        Money discount = total.multiply(DISCOUNT_RATE);
        return total.subtract(discount);
    }
}
