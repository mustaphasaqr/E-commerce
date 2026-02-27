package com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Payment Gateway Client (Generic for Egyptian Payment Gateways)
 * Responsibility: Communicate with payment gateway API
 * 
 * Supported Egyptian Payment Gateways:
 * - Paymob / Accept (Egyptian fintech, excellent API)
 * - Fawry (Most popular Egyptian gateway)
 * - PayTabs (MENA region)
 * - PayFort by Amazon (MENA region)
 * - Vodafone Cash, Masary, Aman (Wallets)
 * 
 * NOTE: This is a stub implementation for development.
 * Production: Replace with real SDK:
 * - Paymob: com.paymob:paymob-java
 * - Fawry: https://developer.fawry.com/
 * - PayTabs: https://site.paytabs.com/en/developers/
 * 
 * Features:
 * - Idempotency support (prevents duplicate charges/refunds)
 * - In-memory idempotency store (production: use Redis/Database)
 * - Works with any gateway that supports idempotency keys
 */
@Component("paymentGatewayClient")
public class PaymentGatewayClient {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayClient.class);
    
    // In-memory idempotency store (production: use Redis or database)
    private final Map<String, String> idempotencyStore = new ConcurrentHashMap<>();

    /**
     * Legacy method without idempotency (deprecated)
     */
    @Deprecated
    public String charge(double amount, String paymentToken) {
        // Delegate to idempotent version with generated key
        return chargeWithIdempotency(amount, paymentToken, "legacy_" + UUID.randomUUID());
    }

    /**
     * Charge with idempotency key
     * Same idempotency key will return the same transaction ID (safe to retry)
     * Thread-safe: uses atomic putIfAbsent for concurrent requests
     */
    public String chargeWithIdempotency(double amount, String paymentToken, String idempotencyKey) {
        // Check if this request was already processed
        String existingTxnId = idempotencyStore.get(idempotencyKey);
        if (existingTxnId != null) {
            logger.info("Idempotent charge detected: returning existing transactionId={}", existingTxnId);
            return existingTxnId;
        }
        
        // TODO: Implement real payment gateway API call
        // 
        // Production Examples:
        // 
        // Paymob:
        //   PaymobClient client = new PaymobClient(apiKey);
        //   PaymentRequest request = PaymentRequest.builder()
        //       .amount(amount)
        //       .currency("EGP")
        //       .paymentToken(paymentToken)
        //       .idempotencyKey(idempotencyKey)
        //       .build();
        //   PaymentResponse response = client.charge(request);
        //   return response.getTransactionId();
        // 
        // Fawry:
        //   FawryClient client = new FawryClient(merchantCode, securityKey);
        //   ChargeRequest request = new ChargeRequest(amount, paymentToken, idempotencyKey);
        //   ChargeResponse response = client.chargeCustomer(request);
        //   return response.getReferenceNumber();
        
        logger.info("Charging {} EGP via Payment Gateway with token: {}, idempotencyKey: {}", 
                   amount, paymentToken, idempotencyKey);
        
        // Generate transaction ID
        String transactionId = "txn_" + UUID.randomUUID().toString();
        
        // Atomically store: if another thread beat us, use their result
        String previousTxnId = idempotencyStore.putIfAbsent(idempotencyKey, transactionId);
        if (previousTxnId != null) {
            logger.info("Concurrent idempotent charge: another thread created transactionId={}", previousTxnId);
            return previousTxnId;
        }
        
        return transactionId;
    }

    /**
     * Legacy method without idempotency (deprecated)
     */
    @Deprecated
    public String refund(String orderId, double amount) {
        // Delegate to idempotent version
        return refundWithIdempotency(orderId, amount, "legacy_refund_" + UUID.randomUUID());
    }

    /**
     * Refund with idempotency key
     * Same idempotency key will return the same refund ID (safe to retry)
     * Thread-safe: uses atomic putIfAbsent for concurrent requests
     */
    public String refundWithIdempotency(String orderId, double amount, String idempotencyKey) {
        // Check if this refund was already processed
        String existingRefundId = idempotencyStore.get(idempotencyKey);
        if (existingRefundId != null) {
            logger.info("Idempotent refund detected: returning existing refundId={}", existingRefundId);
            return existingRefundId;
        }
        
        // TODO: Implement real payment gateway refund API
        // 
        // Production Examples:
        // 
        // Paymob:
        //   RefundRequest request = RefundRequest.builder()
        //       .transactionId(orderId)
        //       .amount(amount)
        //       .idempotencyKey(idempotencyKey)
        //       .build();
        //   RefundResponse response = client.refund(request);
        //   return response.getRefundId();
        // 
        // Fawry:
        //   RefundRequest request = new RefundRequest(orderId, amount, idempotencyKey);
        //   RefundResponse response = client.refundPayment(request);
        //   return response.getReferenceNumber();
        
        logger.info("Refunding {} EGP for order: {}, idempotencyKey: {}", 
                   amount, orderId, idempotencyKey);
        
        // Generate refund ID
        String refundId = "refund_" + UUID.randomUUID().toString();
        
        // Atomically store: if another thread beat us, use their result
        String previousRefundId = idempotencyStore.putIfAbsent(idempotencyKey, refundId);
        if (previousRefundId != null) {
            logger.info("Concurrent idempotent refund: another thread created refundId={}", previousRefundId);
            return previousRefundId;
        }
        
        return refundId;
    }
    
    /**
     * Clear idempotency store (for testing)
     */
    public void clearIdempotencyStore() {
        idempotencyStore.clear();
    }
}
