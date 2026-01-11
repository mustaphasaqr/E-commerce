package com.mustapha.ecommerce.order.application.strategy;

import java.util.Map;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Payment Strategy Interface
 * Pattern: Strategy
 */
public interface PaymentStrategy {
    void processPayment(Money amount, Map<String, String> paymentDetails);
}
