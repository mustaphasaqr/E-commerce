package com.mustapha.ecommerce.order.infrastructure.adapter.payment;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk.StripeClient;

/**
 * Payment Adapter
 * Responsibility: Implement PaymentPort using external payment services (Stripe, PayPal)
 * Pattern: Adapter (Hexagonal Architecture)
 */
@Component
@Primary
public class PaymentAdapter implements PaymentPort {

    private final StripeClient stripeClient;

    public PaymentAdapter(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public PaymentResult processPayment(OrderId orderId, Money amount, String paymentMethod, String paymentToken) {
        try {
            // Delegate to external payment SDK (Stripe)
            String transactionId = stripeClient.charge(amount.getAmount(), paymentToken);
            return new PaymentResult(true, transactionId, "Payment processed successfully");
        } catch (Exception e) {
            return new PaymentResult(false, null, "Payment failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult refundPayment(OrderId orderId, Money amount) {
        try {
            // Delegate to Stripe refund API
            String transactionId = stripeClient.refund(orderId.getValue(), amount.getAmount());
            return new PaymentResult(true, transactionId, "Refund processed successfully");
        } catch (Exception e) {
            return new PaymentResult(false, null, "Refund failed: " + e.getMessage());
        }
    }
}
