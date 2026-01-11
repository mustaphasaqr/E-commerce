package com.mustapha.ecommerce.order.application.strategy;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

import java.util.Map;

/**
 * Wallet Payment Strategy
 * Pattern: Strategy
 */
@Component
public class WalletStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Money amount, Map<String, String> paymentDetails) {
        // Implement wallet payment logic
        System.out.println("Processing wallet payment: " + amount);
    }
}
