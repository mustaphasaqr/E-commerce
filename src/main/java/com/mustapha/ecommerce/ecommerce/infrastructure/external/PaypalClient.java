package com.mustapha.ecommerce.ecommerce.infrastructure.external;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PayPal Client
 * Responsibility: External PayPal integration
 */
@Component
public class PaypalClient {

    public void executePayment(double amount, Map<String, String> details) {
        // PayPal API integration
        System.out.println("Processing PayPal payment: " + amount);
    }
}
