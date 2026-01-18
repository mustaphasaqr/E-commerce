package com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stripe Client SDK
 * Responsibility: Communicate with Stripe payment gateway
 * 
 * NOTE: This is a stub implementation for development.
 * Production: Use real Stripe SDK (com.stripe:stripe-java)
 */
@Component("orderStripeClient")
public class StripeClient {

    public String charge(double amount, String paymentToken) {
        // TODO: Implement real Stripe API call
        // Production: use com.stripe.model.Charge.create()
        System.out.println("Charging " + amount + " via Stripe with token: " + paymentToken);
        
        // Stub: Return mock transaction ID
        return "txn_" + UUID.randomUUID().toString();
    }

    public String refund(String orderId, double amount) {
        // TODO: Implement real Stripe refund API
        System.out.println("Refunding " + amount + " for order: " + orderId);
        
        // Stub: Return mock refund ID
        return "refund_" + UUID.randomUUID().toString();
    }
}
