package com.mustapha.ecommerce.ecommerce.order.domain.service;

import org.springframework.stereotype.Service;

import com.mustapha.ecommerce.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;

/**
 * Pricing Service - Domain Service
 * Responsibility: Orchestrate pricing logic
 * Pattern: Domain Service
 */
@Service
public class PricingService {

    private final DiscountPolicy discountPolicy;

    public PricingService(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public Money calculateFinalPrice(Order order) {
        // Calculate base price from items
        Money basePrice = order.getTotalAmount();
        
        // Apply discount policy
        Money finalPrice = discountPolicy.applyDiscount(order);
        
        return finalPrice;
    }
}
