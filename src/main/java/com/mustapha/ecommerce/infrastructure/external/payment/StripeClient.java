package com.mustapha.ecommerce.infrastructure.external.payment;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Stripe Client
 * Responsibility: External Stripe integration
 */
@Component
public class StripeClient {

    public void processPayment(double amount, Map<String, String> details) {
        // Stripe API integration
        System.out.println("Processing Stripe payment: " + amount);
    }
}
