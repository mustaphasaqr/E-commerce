package com.mustapha.ecommerce.ecommerce.order.infrastructure.adapter.payment;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.ecommerce.order.infrastructure.adapter.payment.sdk.StripeClient;

import java.util.Map;

/**
 * Payment Adapter
 * Responsibility: Implement PaymentPort using external payment services
 * Pattern: Adapter (Hexagonal Architecture)
 */
@Component
public class PaymentAdapter implements PaymentPort {

    private final StripeClient stripeClient;

    public PaymentAdapter(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public void processPayment(Money amount, Map<String, String> paymentDetails) {
        // Delegate to external payment SDK
        stripeClient.charge(amount.getAmount(), paymentDetails);
    }
}
