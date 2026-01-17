package com.mustapha.ecommerce.order.application.port.fraudcheck;

/**
 * Fraud Risk Level Enum
 * Represents the risk assessment level for fraud detection
 */
public enum FraudRiskLevel {
    /**
     * Low risk - Safe to proceed with order
     */
    LOW,
    
    /**
     * Medium risk - Requires manual review
     */
    MEDIUM,
    
    /**
     * High risk - Block transaction immediately
     */
    HIGH
}
