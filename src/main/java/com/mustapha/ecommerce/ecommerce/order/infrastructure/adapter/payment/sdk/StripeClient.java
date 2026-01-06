package com.mustapha.ecommerce.ecommerce.order.infrastructure.adapter.payment.sdk;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Stripe Client SDK
 * Responsibility: Communicate with Stripe payment gateway
 */
@Component
public class StripeClient {

    public void charge(double amount, Map<String, String> paymentDetails) {
        // Implement Stripe API call
        System.out.println("Charging " + amount + " via Stripe");
        // Call Stripe API here
    }
}
