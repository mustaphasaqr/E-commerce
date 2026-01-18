package com.mustapha.ecommerce.infrastructure.external;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PayPal Client (Legacy)
 * Responsibility: External PayPal integration
 */
@Component("legacyPaypalClient")
public class PaypalClient {

    public void executePayment(double amount, Map<String, String> details) {
        // PayPal API integration
        System.out.println("Processing PayPal payment: " + amount);
    }
}
