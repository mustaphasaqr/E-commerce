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
 * Responsibility: Implement PaymentPort using external payment services
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * Egyptian Payment Gateways:
 * - Paymob/Accept (recommended for Egypt)
 * - Fawry (most popular)
 * - PayTabs (MENA)
 * - PayFort (Amazon)
 * 
 * Resilience Features:
 * - @Retry: Automatic retry on transient failures (3 attempts, exponential backoff)
 * - @CircuitBreaker: Stops calling gateway when failure rate exceeds 50%
 * - Idempotency: Uses orderId as idempotency key to prevent duplicate charges
 * - Fallback: Returns error response when circuit is open
 */
@Component
@Primary
public class PaymentAdapter implements PaymentPort {

    private static final Logger logger = LoggerFactory.getLogger(PaymentAdapter.class);
    private final PaymentGatewayClient paymentGatewayClient;

    public PaymentAdapter(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
    }

    /**
     * Process payment with resilience patterns:
     * 1. Retry on transient failures (network timeout, temporary errors)
     * 2. Circuit breaker to prevent cascade failures
     * 3. Idempotency key to prevent duplicate charges
     */
    @Override
    @Retry(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @CircuitBreaker(name = "paymentService")
    public PaymentResult processPayment(OrderId orderId, Money amount, String paymentMethod, String paymentToken) {
        try {
            // Generate idempotency key from orderId to prevent duplicate charges
            String idempotencyKey = "payment_" + orderId.getValue();
            
            logger.debug("Processing payment for order: {}, amount: {}, idempotencyKey: {}", 
                        orderId.getValue(), amount.getAmount(), idempotencyKey);
            
            // Delegate to external payment gateway SDK with idempotency key
            String transactionId = paymentGatewayClient.chargeWithIdempotency(
                amount.getAmount(), 
                paymentToken, 
                idempotencyKey
            );
            
            logger.info("Payment successful: orderId={}, transactionId={}", orderId.getValue(), transactionId);
            return new PaymentResult(true, transactionId, "Payment processed successfully");
            
        } catch (Exception e) {
            logger.error("Payment failed for order: {}, error: {}", orderId.getValue(), e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback method for payment processing
     * Called when:
     * - All retry attempts exhausted
     * - Circuit breaker is OPEN
     * - Exception occurs during processing
     */
    private PaymentResult processPaymentFallback(OrderId orderId, Money amount, 
                                                 String paymentMethod, String paymentToken, 
                                                 Throwable throwable) {
        logger.error("Payment fallback triggered for order: {}, reason: {}", 
                    orderId.getValue(), throwable.getMessage());
        
        return new PaymentResult(
            false, 
            null, 
            "Payment service temporarily unavailable. Please try again later. (Ref: " + orderId.getValue() + ")"
        );
    }

    /**
     * Process refund with resilience patterns:
     * 1. Retry on transient failures
     * 2. Circuit breaker
     * 3. Idempotency key to prevent duplicate refunds
     */
    @Override
    @Retry(name = "paymentService", fallbackMethod = "refundPaymentFallback")
    @CircuitBreaker(name = "paymentService")
    public PaymentResult refundPayment(OrderId orderId, Money amount) {
        try {
            // Generate idempotency key from orderId to prevent duplicate refunds
            String idempotencyKey = "refund_" + orderId.getValue();
            
            logger.debug("Processing refund for order: {}, amount: {}, idempotencyKey: {}", 
                        orderId.getValue(), amount.getAmount(), idempotencyKey);
            
            // Delegate to payment gateway refund API with idempotency
            String transactionId = paymentGatewayClient.refundWithIdempotency(
                orderId.getValue(), 
                amount.getAmount(), 
                idempotencyKey
            );
            
            logger.info("Refund successful: orderId={}, transactionId={}", orderId.getValue(), transactionId);
            return new PaymentResult(true, transactionId, "Refund processed successfully");
            
        } catch (Exception e) {
            logger.error("Refund failed for order: {}, error: {}", orderId.getValue(), e.getMessage());
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback method for refund processing
     */
    private PaymentResult refundPaymentFallback(OrderId orderId, Money amount, Throwable throwable) {
        logger.error("Refund fallback triggered for order: {}, reason: {}", 
                    orderId.getValue(), throwable.getMessage());
        
        return new PaymentResult(
            false, 
            null, 
            "Refund service temporarily unavailable. Please contact support. (Ref: " + orderId.getValue() + ")"
        );
    }
}
