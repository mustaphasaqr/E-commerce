package com.mustapha.ecommerce.ecommerce.order.application.port;

import java.util.Map;

import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;

/**
 * Payment Port - Hexagonal Architecture
 * Responsibility: Define contract for payment processing
 */
public interface PaymentPort {
    void processPayment(Money amount, Map<String, String> paymentDetails);
}
