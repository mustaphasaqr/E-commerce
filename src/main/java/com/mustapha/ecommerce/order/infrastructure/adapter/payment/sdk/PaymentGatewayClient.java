package com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk;

/**
 * Payment Gateway Client Interface
 * 
 * Generic interface for payment gateway implementations (Accept, Fawry, PayTabs, etc.)
 * Provides both modern checkout flow and legacy charge operations
 * 
 * This interface supports two payment flows:
 * 1. Checkout Flow: createCheckout() → redirect customer → verifyPayment()
 * 2. Legacy Flow: charge() / chargeWithIdempotency() for direct charges
 * 
 * Real implementations: AcceptPaymobClient, FawryClient, etc.
 */
public interface PaymentGatewayClient {
    
    /**
     * Create checkout session for payment
     * Returns payment key/URL for customer to complete payment
     * 
     * @param orderId Unique order identifier
     * @param amount Amount in cents/piastres
     * @param currency Currency code (EGP, SAR, AED)
     * @param customerEmail Customer email for receipt
     * @param customerPhone Customer phone (optional)
     * @return CheckoutResponse with payment key and redirect URL
     */
    CheckoutResponse createCheckout(
            String orderId,
            double amount,
            String currency,
            String customerEmail,
            String customerPhone
    );
    
    /**
     * Verify payment status after customer completes payment
     * 
     * @param transactionId Transaction ID from payment gateway
     * @return Payment verification result
     */
    PaymentVerificationResponse verifyPayment(String transactionId);
    
    /**
     * Nested class: Checkout Response
     */
    record CheckoutResponse(
            String paymentKey,
            String error,
            int expiresInSeconds  // Time in seconds until payment key expires
    ) {}
    
    /**
     * Nested class: Payment Verification Response
     */
    record PaymentVerificationResponse(
            boolean success,
            String status,          // PAID, PENDING, FAILED, CANCELLED
            String transactionId,
            String orderId          // Can be null if verification fails
    ) {}
    
    /**
     * Charge payment with idempotency key
     * Safe to retry - same idempotency key returns same result
     * 
     * @param amount Amount to charge
     * @param paymentToken Payment method token (card token, wallet ID, etc.)
     * @param idempotencyKey Unique key for this payment attempt
     * @return Transaction ID if successful
     * @throws RuntimeException if payment fails
     */
    String chargeWithIdempotency(double amount, String paymentToken, String idempotencyKey);
    
    /**
     * Refund payment with idempotency key
     * Safe to retry - same idempotency key returns same result
     * 
     * @param transactionId Original transaction ID to refund
     * @param amount Amount to refund (can be partial)
     * @param idempotencyKey Unique key for this refund attempt
     * @return Refund ID if successful
     * @throws RuntimeException if refund fails
     */
    String refundWithIdempotency(String transactionId, double amount, String idempotencyKey);
    
    /**
     * Charge payment (legacy method without explicit idempotency)
     * Generates idempotency key automatically
     * 
     * @param amount Amount to charge
     * @param paymentToken Payment method token
     * @return Transaction ID if successful
     * @throws RuntimeException if payment fails
     */
    default String charge(double amount, String paymentToken) {
        // Generate automatic idempotency key based on amount and token
        String idempotencyKey = "auto_" + paymentToken + "_" + System.currentTimeMillis();
        return chargeWithIdempotency(amount, paymentToken, idempotencyKey);
    }
    
    /**
     * Refund payment (legacy method without explicit idempotency)
     * Generates idempotency key automatically
     * 
     * @param transactionId Original transaction ID to refund
     * @param amount Amount to refund
     * @return Refund ID if successful
     * @throws RuntimeException if refund fails
     */
    default String refund(String transactionId, double amount) {
        // Generate automatic idempotency key
        String idempotencyKey = "auto_refund_" + transactionId + "_" + System.currentTimeMillis();
        return refundWithIdempotency(transactionId, amount, idempotencyKey);
    }
    
    /**
     * Clear idempotency store (for testing purposes)
     * In production, use time-based expiration instead
     */
    void clearIdempotencyStore();
}
