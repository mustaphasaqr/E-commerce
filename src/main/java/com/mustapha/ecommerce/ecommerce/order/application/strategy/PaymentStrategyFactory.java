package com.mustapha.ecommerce.ecommerce.order.application.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Payment Strategy Factory
 * Pattern: Factory
 * SOLID: OCP
 */
@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(CreditCardStrategy creditCardStrategy, 
                                 WalletStrategy walletStrategy) {
        this.strategies = Map.of(
            "CREDIT_CARD", creditCardStrategy,
            "WALLET", walletStrategy
        );
    }

    public PaymentStrategy getStrategy(String paymentMethod) {
        PaymentStrategy strategy = strategies.get(paymentMethod);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return strategy;
    }
}
