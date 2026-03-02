package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Fraud Check Port (Outbound Port)
 * Responsibility: Assess transaction risk and detect fraudulent activity
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation strategies:
 * - Rule-based internal checks (velocity, amount, patterns)
 * - External fraud detection APIs (Stripe Radar, Sift, Forter, Riskified)
 * - Machine learning models
 * - IP geolocation checks (MaxMind)
 * 
 * Use cases:
 * - Before payment authorization (prevent fraudulent charges)
 * - After order placement (flag suspicious orders for manual review)
 * - Continuous monitoring (detect account takeover)
 */
public interface FraudCheckPort {
    
    /**
     * Assess fraud risk for an order before payment
     * Called by PlaceOrderUseCase or ProcessPaymentUseCase
     * 
     * @param request Fraud assessment request with order details
     * @return Risk assessment result with score and recommendation
     */
    FraudAssessment assessOrderRisk(FraudCheckRequest request);
    
    /**
     * Report a confirmed fraudulent transaction
     * Helps improve fraud detection models (feedback loop)
     * 
     * @param orderId Order that was confirmed as fraud
     * @param reason Reason for fraud classification
     */
    void reportFraud(OrderId orderId, String reason);
    
    /**
     * Report a false positive (legitimate transaction flagged as fraud)
     * Helps improve accuracy (reduce false positives)
     * 
     * @param orderId Order that was incorrectly flagged
     */
    void reportFalsePositive(OrderId orderId);
    
    // ========== Request/Response DTOs ==========
    
    /**
     * Fraud Check Request
     * Contains all data points needed for risk assessment
     * 
     * @param orderId Order being assessed
     * @param customerId Customer placing the order
     * @param orderAmount Total order value
     * @param customerEmail Customer email address
     * @param customerPhone Customer phone number (optional)
     * @param ipAddress IP address of the request
     * @param userAgent Browser user agent string
     * @param shippingCountry Delivery country code (ISO 3166-1 alpha-2)
     * @param billingCountry Billing country code (if different from shipping)
     * @param isNewCustomer True if first order from this customer
     * @param paymentMethod Payment method (CARD, WALLET, COD, etc.)
     * @param deviceFingerprint Unique device identifier (optional)
     */
    record FraudCheckRequest(
        OrderId orderId,
        CustomerId customerId,
        Money orderAmount,
        String customerEmail,
        String customerPhone,
        String ipAddress,
        String userAgent,
        String shippingCountry,
        String billingCountry,
        boolean isNewCustomer,
        String paymentMethod,
        String deviceFingerprint
    ) {
        public FraudCheckRequest {
            if (orderId == null) throw new IllegalArgumentException("Order ID required");
            if (customerId == null) throw new IllegalArgumentException("Customer ID required");
            if (orderAmount == null) throw new IllegalArgumentException("Order amount required");
        }
    }
    
    /**
     * Fraud Assessment Result
     * Contains risk score and recommendation
     * 
     * Risk levels:
     * - LOW (0-30): Accept automatically
     * - MEDIUM (31-70): Require additional verification (3DS, phone verification)
     * - HIGH (71-100): Reject or hold for manual review
     * 
     * @param riskScore Risk score from 0 (safest) to 100 (most risky)
     * @param riskLevel Risk level classification
     * @param recommendation Action to take (ACCEPT, REVIEW, REJECT, CHALLENGE)
     * @param reasons List of risk factors detected (e.g., "High velocity", "New customer")
     * @param details Additional details for manual review
     */
    record FraudAssessment(
        int riskScore,           // 0-100
        RiskLevel riskLevel,
        Recommendation recommendation,
        java.util.List<String> reasons,
        String details
    ) {
        public FraudAssessment {
            if (riskScore < 0 || riskScore > 100) {
                throw new IllegalArgumentException("Risk score must be 0-100");
            }
            if (riskLevel == null) throw new IllegalArgumentException("Risk level required");
            if (recommendation == null) throw new IllegalArgumentException("Recommendation required");
            if (reasons == null) reasons = java.util.Collections.emptyList();
        }
        
        /**
         * Check if transaction is safe to proceed
         * @return true if risk is acceptable (LOW or MEDIUM with ACCEPT)
         */
        public boolean isSafe() {
            return recommendation == Recommendation.ACCEPT;
        }
        
        /**
         * Check if transaction requires manual review
         * @return true if human intervention needed
         */
        public boolean requiresReview() {
            return recommendation == Recommendation.REVIEW;
        }
        
        /**
         * Check if transaction should be rejected
         * @return true if transaction is too risky
         */
        public boolean shouldReject() {
            return recommendation == Recommendation.REJECT;
        }
    }
    
    /**
     * Risk Level Classification
     */
    enum RiskLevel {
        LOW("Low risk - Accept automatically"),
        MEDIUM("Medium risk - Additional verification recommended"),
        HIGH("High risk - Manual review or rejection");
        
        private final String description;
        
        RiskLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Risk-based Recommendation
     */
    enum Recommendation {
        ACCEPT("Accept transaction - risk is acceptable"),
        CHALLENGE("Challenge with additional verification (3DS, OTP)"),
        REVIEW("Hold for manual review by fraud team"),
        REJECT("Reject transaction - too risky");
        
        private final String description;
        
        Recommendation(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
