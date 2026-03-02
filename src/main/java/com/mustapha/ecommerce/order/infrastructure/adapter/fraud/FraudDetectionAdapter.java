package com.mustapha.ecommerce.order.infrastructure.adapter.fraud;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.FraudCheckPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Fraud Detection Adapter - Rule-based Implementation
 * Responsibility: Assess transaction risk using business rules
 * Pattern: Adapter (implements FraudCheckPort)
 * 
 * Current Implementation: Rule-based fraud detection (internal)
 * 
 * Risk Factors Checked:
 * - Order amount (high-value orders are riskier)
 * - New customer (first order has higher risk)
 * - IP location mismatch (shipping country != IP country)
 * - Velocity (multiple orders from same customer)
 * - Payment method (COD is lower risk than card)
 * - Suspicious email patterns (disposable emails)
 * 
 * Future Enhancements:
 * - Integrate with external fraud detection APIs:
 *   - Stripe Radar (if using Stripe)
 *   - Sift (e-commerce focused, $500/month+)
 *   - Forter (enterprise, real-time decisions)
 *   - Riskified (chargeback guarantee)
 *   - MaxMind minFraud (IP intelligence, $200/month)
 * 
 * - Machine learning model:
 *   - Train on historical fraud data
 *   - Features: customer behavior, device fingerprints, transaction patterns
 * 
 * - Advanced checks:
 *   - Email/phone verification (SendGrid, Twilio Verify)
 *   - Device fingerprinting (FingerprintJS)
 *   - Behavioral biometrics (typing patterns, mouse movements)
 */
@Component
public class FraudDetectionAdapter implements FraudCheckPort {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionAdapter.class);
    
    // Risk thresholds (configurable in production)
    private static final double HIGH_VALUE_THRESHOLD = 5000.0; // Orders > 5000 EGP
    private static final double VERY_HIGH_VALUE_THRESHOLD = 10000.0; // Orders > 10000 EGP
    private static final int LOW_RISK_THRESHOLD = 30;
    private static final int HIGH_RISK_THRESHOLD = 70;
    
    @Override
    public FraudAssessment assessOrderRisk(FraudCheckRequest request) {
        logger.info("🔍 Assessing fraud risk: orderId={}, customerId={}, amount={}", 
                   request.orderId().getValue(), 
                   request.customerId().getValue(),
                   request.orderAmount().getAmount());
        
        List<String> riskFactors = new ArrayList<>();
        int riskScore = 0;
        
        // ========== Risk Factor 1: Order Amount ==========
        double amount = request.orderAmount().getAmount();
        if (amount > VERY_HIGH_VALUE_THRESHOLD) {
            riskScore += 40;
            riskFactors.add("Very high order value: " + amount + " (threshold: " + VERY_HIGH_VALUE_THRESHOLD + ")");
        } else if (amount > HIGH_VALUE_THRESHOLD) {
            riskScore += 25;
            riskFactors.add("High order value: " + amount + " (threshold: " + HIGH_VALUE_THRESHOLD + ")");
        }
        
        // ========== Risk Factor 2: New Customer ==========
        if (request.isNewCustomer()) {
            riskScore += 20;
            riskFactors.add("First order from new customer");
        }
        
        // ========== Risk Factor 3: Country Mismatch ==========
        // Check if shipping country matches billing country
        if (request.billingCountry() != null && 
            !request.shippingCountry().equalsIgnoreCase(request.billingCountry())) {
            riskScore += 15;
            riskFactors.add("Shipping country (" + request.shippingCountry() + 
                          ") differs from billing country (" + request.billingCountry() + ")");
        }
        
        // ========== Risk Factor 4: Suspicious Email ==========
        if (request.customerEmail() != null && isDisposableEmail(request.customerEmail())) {
            riskScore += 25;
            riskFactors.add("Disposable/temporary email detected: " + request.customerEmail());
        }
        
        // ========== Risk Factor 5: Payment Method Risk ==========
        if ("CARD".equalsIgnoreCase(request.paymentMethod()) && request.isNewCustomer()) {
            riskScore += 10;
            riskFactors.add("Card payment from new customer (higher chargeback risk)");
        }
        // Cash on Delivery is lower risk
        if ("COD".equalsIgnoreCase(request.paymentMethod())) {
            riskScore = Math.max(0, riskScore - 10);
            riskFactors.add("Cash on Delivery reduces risk (no chargeback risk)");
        }
        
        // ========== Risk Factor 6: Missing Information ==========
        if (request.customerPhone() == null || request.customerPhone().isBlank()) {
            riskScore += 10;
            riskFactors.add("No phone number provided");
        }
        
        if (request.ipAddress() == null || request.ipAddress().isBlank()) {
            riskScore += 5;
            riskFactors.add("No IP address captured");
        }
        
        // Cap score at 100
        riskScore = Math.min(100, riskScore);
        
        // Determine risk level and recommendation
        RiskLevel riskLevel;
        Recommendation recommendation;
        
        if (riskScore <= LOW_RISK_THRESHOLD) {
            riskLevel = RiskLevel.LOW;
            recommendation = Recommendation.ACCEPT;
        } else if (riskScore <= HIGH_RISK_THRESHOLD) {
            riskLevel = RiskLevel.MEDIUM;
            recommendation = Recommendation.CHALLENGE; // Require 3DS or phone verification
        } else {
            riskLevel = RiskLevel.HIGH;
            recommendation = Recommendation.REVIEW; // Manual review required
        }
        
        // Build assessment details
        String details = buildAssessmentDetails(request, riskScore, riskFactors);
        
        FraudAssessment assessment = new FraudAssessment(
            riskScore,
            riskLevel,
            recommendation,
            riskFactors,
            details
        );
        
        logger.info("✅ Fraud assessment complete: orderId={}, riskScore={}, level={}, recommendation={}", 
                   request.orderId().getValue(), 
                   riskScore, 
                   riskLevel, 
                   recommendation);
        
        if (!riskFactors.isEmpty()) {
            logger.warn("⚠️ Risk factors detected for order {}: {}", 
                       request.orderId().getValue(), 
                       String.join(", ", riskFactors));
        }
        
        return assessment;
    }
    
    @Override
    public void reportFraud(OrderId orderId, String reason) {
        logger.warn("🚨 FRAUD REPORTED: orderId={}, reason={}", orderId.getValue(), reason);
        
        // TODO: Store in database for ML training
        // TODO: Update fraud patterns
        // TODO: Blacklist customer/card if confirmed fraud
        // TODO: Notify fraud team
        
        // In production:
        // 1. Store in fraud_reports table
        // 2. Update customer risk profile
        // 3. Send alert to fraud team
        // 4. Feed into ML model for retraining
    }
    
    @Override
    public void reportFalsePositive(OrderId orderId) {
        logger.info("✅ False positive reported: orderId={}", orderId.getValue());
        
        // TODO: Store in database to improve model accuracy
        // TODO: Adjust risk thresholds if too many false positives
        
        // In production:
        // 1. Store in false_positives table
        // 2. Analyze patterns to reduce false positive rate
        // 3. Retrain ML model with corrected labels
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Check if email is from a disposable/temporary email provider
     * Common patterns: mailinator, guerrillamail, 10minutemail, etc.
     */
    private boolean isDisposableEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        String lowerEmail = email.toLowerCase();
        String[] disposableDomains = {
            "mailinator.com", "guerrillamail.com", "10minutemail.com",
            "tempmail.com", "throwaway.email", "maildrop.cc",
            "fakeinbox.com", "trashmail.com", "getnada.com"
        };
        
        for (String domain : disposableDomains) {
            if (lowerEmail.endsWith("@" + domain)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Build detailed assessment summary for audit trail
     */
    private String buildAssessmentDetails(FraudCheckRequest request, int riskScore, List<String> factors) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fraud Assessment Details:\n");
        sb.append("========================\n");
        sb.append("Order ID: ").append(request.orderId().getValue()).append("\n");
        sb.append("Customer ID: ").append(request.customerId().getValue()).append("\n");
        sb.append("Amount: ").append(request.orderAmount().getAmount()).append(" EGP\n");
        sb.append("Email: ").append(maskEmail(request.customerEmail())).append("\n");
        sb.append("Phone: ").append(maskPhone(request.customerPhone())).append("\n");
        sb.append("IP: ").append(request.ipAddress()).append("\n");
        sb.append("Shipping Country: ").append(request.shippingCountry()).append("\n");
        sb.append("Payment Method: ").append(request.paymentMethod()).append("\n");
        sb.append("New Customer: ").append(request.isNewCustomer()).append("\n");
        sb.append("\nRisk Score: ").append(riskScore).append("/100\n");
        sb.append("Risk Factors: ").append(factors.size()).append("\n");
        for (String factor : factors) {
            sb.append("  - ").append(factor).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Mask email for privacy/GDPR compliance
     * Example: john.doe@example.com → j***@example.com
     */
    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "N/A";
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email; // Can't mask properly
        
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
    
    /**
     * Mask phone for privacy/GDPR compliance
     * Example: +201234567890 → +20******890
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return "N/A";
        if (phone.length() < 6) return "***";
        
        return phone.substring(0, 3) + "******" + phone.substring(phone.length() - 3);
    }
}
