package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Payment Port (Outbound Port)
 * Responsibility: Process payments through external payment gateway
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: StripePaymentService, PayPalPaymentService, BraintreePaymentService
 */
public interface PaymentPort {
    
    /**
     * Process payment for an order
     * 
     * @param orderId Order being paid for
     * @param amount Amount to charge
     * @param paymentMethod Payment method (e.g., "credit_card", "paypal")
     * @param paymentToken Token/details from payment gateway
     * @return Payment result with status and transaction ID
     * @throws PaymentFailedException if payment processing fails
     */
    PaymentResult processPayment(OrderId orderId, Money amount, String paymentMethod, String paymentToken);
    
    /**
     * Refund payment for a cancelled order
     * 
     * @param orderId Order being refunded
     * @param amount Amount to refund
     * @return Payment result with status and transaction ID
     */
    PaymentResult refundPayment(OrderId orderId, Money amount);
    
    /**
     * Payment Result
     * Encapsulates the result of a payment processing attempt
     */
    record PaymentResult(
        boolean success,
        String transactionId,
        String message
    ) {
        public PaymentResult {
            if (success && (transactionId == null || transactionId.isBlank())) {
                throw new IllegalArgumentException("Transaction ID cannot be null or blank for successful payments");
            }
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public boolean isFailed() {
            return !success;
        }
        
        public String errorMessage() {
            return message;
        }
    }
}
