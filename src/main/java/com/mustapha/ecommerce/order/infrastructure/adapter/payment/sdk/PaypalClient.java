package com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PayPal Client SDK
 * Responsibility: Communicate with PayPal payment gateway
 */
@Component
public class PaypalClient {

    public void executePayment(double amount, Map<String, String> paymentDetails) {
        // Implement PayPal API call
        System.out.println("Processing " + amount + " via PayPal");
        // Call PayPal API here
    }
}
