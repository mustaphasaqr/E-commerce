package com.mustapha.ecommerce.order.domain.service;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Discount Policy Interface
 * Pattern: Strategy
 * Responsibility: Define contract for discount calculation
 */
public interface DiscountPolicy {
    Money applyDiscount(Order order);
}
