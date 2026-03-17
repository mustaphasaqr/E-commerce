package com.mustapha.ecommerce.order.infrastructure.adapter.payment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk.PaymentGatewayClient;

/**
 * Payment Adapter
 * Responsibility: Implement PaymentPort using payment gateway (Accept/Paymob default)
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * Payment Gateway Coverage:
 * - Egypt: Visa, Mastercard, Fawry Pay, Installments
 * - Saudi Arabia: Visa, Mastercard, Mada, Apple Pay
 * - UAE: Visa, Mastercard, Apple Pay
 * - Other regions through partner banks
 * 
 * Resilience Features:
 * - @Retry: Automatic retry on transient failures (3 attempts, exponential backoff)
 * - @CircuitBreaker: Stops calling gateway when failure rate exceeds 50%
 * - Idempotency: Built into gateway client (prevents duplicate checkouts)
 * - Fallback: Returns error response when circuit is open
 * 
 * Design: Depends on PaymentGatewayClient interface for testability and flexibility
 */
@Component
@Primary
public class PaymentAdapter implements PaymentPort {

    private static final Logger logger = LoggerFactory.getLogger(PaymentAdapter.class);
    private final PaymentGatewayClient paymentGatewayClient;
    
    private static final String DEFAULT_CURRENCY = "EGP"; // Egyptian Pound

    @org.springframework.beans.factory.annotation.Value("${payment.accept.iframe-id:1010744}")
    private String iframeId;

    public PaymentAdapter(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
    }

    /**
     * Create checkout session for payment
     * Customer will be redirected to Accept (Paymob) payment page
     * 
     * Resilience: Retry on transient failures, circuit breaker on persistent failures
     */
    @Override
    @Retry(name = "paymentService", fallbackMethod = "createCheckoutFallback")
    @CircuitBreaker(name = "paymentService")
    public CheckoutResult createCheckout(
            OrderId orderId, 
            Money amount, 
            String paymentMethod, 
            String customerEmail) {
        
        try {
            logger.debug("Creating checkout: orderId={}, amount={}, method={}", 
                        orderId.getValue(), amount.getAmount(), paymentMethod);
            
            PaymentGatewayClient.CheckoutResponse response = paymentGatewayClient.createCheckout(
                orderId.getValue(),
                amount.getAmount(),
                DEFAULT_CURRENCY,
                customerEmail,
                null // phone number (optional)
            );
            
            if (response.paymentKey() != null) {
                // Build redirect URL for customer (Accept iframe)
                String redirectUrl = buildPaymentIframeUrl(response.paymentKey());
                
                logger.info("✅ Checkout created: orderId={}, paymentKey={}", 
                           orderId.getValue(), response.paymentKey());
                
                return new CheckoutResult(
                    true,
                    response.paymentKey(), // Using payment key as checkout ID
                    redirectUrl,
                    response.expiresInSeconds(),
                    "Checkout session created successfully"
                );
            } else {
                logger.error("❌ Checkout creation failed: {}", response.error());
                return new CheckoutResult(
                    false,
                    null,
                    null,
                    0,
                    response.error() != null ? response.error() : "Unknown error"
                );
            }
            
        } catch (Exception e) {
            logger.error("❌ Checkout creation error: orderId={}, error={}", 
                        orderId.getValue(), e.getMessage());
            throw new RuntimeException("Checkout creation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback for checkout creation
     */
    private CheckoutResult createCheckoutFallback(
            OrderId orderId, 
            Money amount, 
            String paymentMethod, 
            String customerEmail,
            Throwable throwable) {
        
        logger.error("Checkout fallback triggered: orderId={}, reason={}", 
                    orderId.getValue(), throwable.getMessage());
        
        return new CheckoutResult(
            false,
            null,
            null,
            0,
            "Payment service temporarily unavailable. Please try again later. (Ref: " + orderId.getValue() + ")"
        );
    }
    
    /**
     * Verify payment status after customer returns from gateway
     * 
     * For Accept (Paymob), checkoutId is actually the transaction ID from callback
     * 
     * Resilience: Retry on transient failures, circuit breaker on persistent failures
     */
    @Override
    @Retry(name = "paymentService", fallbackMethod = "verifyPaymentFallback")
    @CircuitBreaker(name = "paymentService")
    public PaymentVerificationResult verifyPayment(String checkoutId) {
        try {
            logger.debug("Verifying payment: transactionId={}", checkoutId);
            
            PaymentGatewayClient.PaymentVerificationResponse response = 
                paymentGatewayClient.verifyPayment(checkoutId);
            
            PaymentStatus status = response.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            
            logger.info("Payment verification: transactionId={}, status={}", checkoutId, status);
            
            return new PaymentVerificationResult(
                response.success(),
                response.transactionId(),
                status,
                response.success() ? "Payment successful" : "Payment failed"
            );
            
        } catch (Exception e) {
            logger.error("❌ Payment verification error: transactionId={}, error={}", 
                        checkoutId, e.getMessage());
            throw new RuntimeException("Payment verification failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback for payment verification
     */
    private PaymentVerificationResult verifyPaymentFallback(String checkoutId, Throwable throwable) {
        logger.error("Payment verification fallback: checkoutId={}, reason={}", 
                    checkoutId, throwable.getMessage());
        
        return new PaymentVerificationResult(
            false,
            null,
            PaymentStatus.FAILED,
            "Payment verification service temporarily unavailable. Please contact support."
        );
    }
    
    /**
     * Refund a completed payment
     * 
     * NOTE: Accept (Paymob) refunds are typically processed through the dashboard
     * API refunds may require additional merchant approval
     * 
     * Resilience: Retry on transient failures, circuit breaker on persistent failures
     */
    @Override
    @Retry(name = "paymentService", fallbackMethod = "refundPaymentFallback")
    @CircuitBreaker(name = "paymentService")
    public RefundResult refundPayment(
            OrderId orderId, 
            String transactionId, 
            Money amount, 
            String reason) {
        
        try {
            logger.warn("Refund requested: orderId={}, transactionId={}, amount={}", 
                        orderId.getValue(), transactionId, amount.getAmount());
            logger.warn("Accept (Paymob) refunds should be processed through dashboard");
            
            // For now, return a message indicating manual processing required
            // In production, you could implement Accept's refund API if available
            
            return new RefundResult(
                false,
                null,
                "Refund must be processed manually through Accept dashboard. Reason: " + reason
            );
            
        } catch (Exception e) {
            logger.error("❌ Refund error: orderId={}, error={}", orderId.getValue(), e.getMessage());
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback for refund processing
     */
    private RefundResult refundPaymentFallback(
            OrderId orderId, 
            String transactionId, 
            Money amount, 
            String reason,
            Throwable throwable) {
        
        logger.error("Refund fallback triggered: orderId={}, reason={}", 
                    orderId.getValue(), throwable.getMessage());
        
        return new RefundResult(
            false,
            null,
            "Refund service temporarily unavailable. Please contact support. (Ref: " + orderId.getValue() + ")"
        );
    }
    
    /**
     * Process payment directly (simplified for testing)
     * 
     * This method is primarily for testing resilience patterns.
     * In production, use createCheckout() + verifyPayment() flow instead.
     * 
     * @param orderId Order being paid
     * @param amount Amount to charge
     * @param paymentMethod Payment method
     * @param paymentToken Payment token (card token, checkout ID, etc.)
     * @return Payment result
     */
    @Retry(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @CircuitBreaker(name = "paymentService")
    public PaymentResult processPayment(
            OrderId orderId,
            Money amount,
            String paymentMethod,
            String paymentToken) {
        
        try {
            logger.debug("Processing payment: orderId={}, amount={}", 
                        orderId.getValue(), amount.getAmount());
            
            // Use checkout flow internally
            PaymentGatewayClient.CheckoutResponse response = paymentGatewayClient.createCheckout(
                orderId.getValue(),
                amount.getAmount(),
                DEFAULT_CURRENCY,
                "test@example.com", // Default email for testing
                null
            );
            
            if (response.paymentKey() != null) {
                logger.info("✅ Payment processed: orderId={}, txnId={}", 
                           orderId.getValue(), response.paymentKey());
                
                return new PaymentResult(
                    true,
                    response.paymentKey(),
                    "Payment processed successfully"
                );
            } else {
                logger.error("❌ Payment failed: {}", response.error());
                return new PaymentResult(
                    false,
                    null,
                    response.error() != null ? response.error() : "Unknown error"
                );
            }
            
        } catch (Exception e) {
            logger.error("❌ Payment error: orderId={}, error={}", 
                        orderId.getValue(), e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback for payment processing
     */
    private PaymentResult processPaymentFallback(
            OrderId orderId,
            Money amount,
            String paymentMethod,
            String paymentToken,
            Throwable throwable) {
        
        logger.error("Payment fallback triggered: orderId={}, reason={}", 
                    orderId.getValue(), throwable.getMessage());
        
        return new PaymentResult(
            false,
            null,
            "Payment service temporarily unavailable. Please try again later."
        );
    }
    
    /**
     * Refund payment (simplified for testing)
     * 2-parameter overload for test compatibility
     * 
     * @param orderId Order being refunded
     * @param amount Amount to refund
     * @return Payment result (simplified)
     */
    @Retry(name = "paymentService", fallbackMethod = "refundPaymentSimpleFallback")
    @CircuitBreaker(name = "paymentService")
    public PaymentResult refundPayment(OrderId orderId, Money amount) {
        try {
            logger.debug("Processing refund: orderId={}, amount={}", 
                        orderId.getValue(), amount.getAmount());
            
            // Use the full refundPayment method
            RefundResult result = refundPayment(
                orderId, 
                "test_txn_" + orderId.getValue(), 
                amount, 
                "Test refund"
            );
            
            return new PaymentResult(
                result.success(),
                result.refundId(),
                result.message()
            );
            
        } catch (Exception e) {
            logger.error("❌ Refund error: orderId={}, error={}", 
                        orderId.getValue(), e.getMessage());
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback for simplified refund processing
     */
    private PaymentResult refundPaymentSimpleFallback(
            OrderId orderId,
            Money amount,
            Throwable throwable) {
        
        logger.error("Refund fallback triggered: orderId={}, reason={}", 
                    orderId.getValue(), throwable.getMessage());
        
        return new PaymentResult(
            false,
            null,
            "Refund service temporarily unavailable. Please contact support."
        );
    }
    
    // Helper methods
    
    private String buildPaymentIframeUrl(String paymentKey) {
        // Accept (Paymob) iframe URL format
        return String.format("https://accept.paymob.com/api/acceptance/iframes/%s?payment_token=%s", 
                           iframeId, paymentKey);
    }
}
