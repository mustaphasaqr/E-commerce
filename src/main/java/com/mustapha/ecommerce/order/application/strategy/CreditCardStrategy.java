package com.mustapha.ecommerce.order.application.strategy;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

import java.util.Map;

/**
 * Credit Card Payment Strategy
 * Pattern: Strategy
 */
@Component
public class CreditCardStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Money amount, Map<String, String> paymentDetails) {
        // Implement credit card payment logic
        System.out.println("Processing credit card payment: " + amount);
    }
}
