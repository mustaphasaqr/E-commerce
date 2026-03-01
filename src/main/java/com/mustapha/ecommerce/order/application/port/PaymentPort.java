package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Payment Port (Outbound Port)
 * Responsibility: Process payments through external payment gateway
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Example: PaymentAdapter → AcceptPaymobClient (Egypt, Saudi Arabia, UAE)
 * 
 * Payment Flow (Checkout-based):
 * 1. createCheckout() → Get redirect URL for customer
 * 2. Customer completes payment on gateway page
 * 3. Gateway redirects back with checkout ID
 * 4. verifyPayment() → Confirm payment status
 * 5. Optional: refundPayment() → Return money if needed
 */
public interface PaymentPort {
    
    /**
     * Create payment checkout session
     * Returns a redirect URL where customer completes payment
     * 
     * @param orderId Order being paid for
     * @param amount Amount to charge
     * @param paymentMethod Payment method (VISA, MASTERCARD, MADA)
     * @param customerEmail Customer email for receipt
     * @return Checkout result with redirect URL
     */
    CheckoutResult createCheckout(
        OrderId orderId,
        Money amount,
        String paymentMethod,
        String customerEmail
    );
    
    /**
     * Verify payment status after customer returns from gateway
     * Call this after customer is redirected back to your site
     * 
     * @param checkoutId Checkout session ID from createCheckout
     * @return Payment verification result
     */
    PaymentVerificationResult verifyPayment(String checkoutId);
    
    /**
     * Refund a completed payment
     * 
     * @param orderId Order being refunded
     * @param transactionId Original transaction ID from verifyPayment
     * @param amount Amount to refund (can be partial)
     * @param reason Refund reason
     * @return Refund result
     */
    RefundResult refundPayment(OrderId orderId, String transactionId, Money amount, String reason);
    
    /**
     * Process payment directly (simplified for testing)
     * Default implementation wraps createCheckout() + immediate success
     * 
     * This method is primarily for backward compatibility with tests.
     * In production, use createCheckout() + verifyPayment() flow instead.
     * 
     * @param orderId Order being paid
     * @param amount Amount to charge
     * @param paymentMethod Payment method
     * @param paymentToken Payment token (card token, checkout ID, etc.)
     * @return Payment result
     */
    default PaymentResult processPayment(
            OrderId orderId,
            Money amount,
            String paymentMethod,
            String paymentToken) {
        // Default implementation for testing - derive from createCheckout
        CheckoutResult checkout = createCheckout(orderId, amount, paymentMethod, "test@example.com");
        if (checkout.success()) {
            return new PaymentResult(
                true,
                checkout.checkoutId(),
                checkout.message()
            );
        } else {
            return new PaymentResult(
                false,
                null,
                checkout.message()
            );
        }
    }
    
    /**
     * Checkout Result
     * Contains redirect URL for customer to complete payment
     */
    record CheckoutResult(
        boolean success,
        String checkoutId,
        String redirectUrl,
        long expiresInSeconds,
        String message
    ) {
        public boolean isSuccess() {
            return success;
        }
        
        public boolean isFailed() {
            return !success;
        }
    }
    
    /**
     * Payment Verification Result
     * Result after verifying payment status with gateway
     */
    record PaymentVerificationResult(
        boolean success,
        String transactionId,
        PaymentStatus status,
        String message
    ) {
        public boolean isSuccess() {
            return success;
        }
        
        public boolean isPending() {
            return status == PaymentStatus.PENDING;
        }
        
        public boolean isFailed() {
            return status == PaymentStatus.FAILED;
        }
    }
    
    /**
     * Refund Result
     */
    record RefundResult(
        boolean success,
        String refundId,
        String message
    ) {
        public boolean isSuccess() {
            return success;
        }
    }
    
    /**
     * Payment Result (for testing/legacy compatibility)
     * Simple result for direct payment operations
     */
    record PaymentResult(
        boolean success,
        String transactionId,
        String message
    ) {
        public boolean isSuccess() {
            return success;
        }
    }
    
    /**
     * Payment Status
     */
    enum PaymentStatus {
        SUCCESS,
        PENDING,
        FAILED,
        CANCELLED
    }
}
