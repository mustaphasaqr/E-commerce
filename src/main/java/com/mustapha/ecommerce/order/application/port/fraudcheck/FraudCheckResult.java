package com.mustapha.ecommerce.order.application.port.fraudcheck;

/**
 * Fraud Check Result
 * Encapsulates the result of a fraud detection check
 * Pattern: Result Object
 */
public record FraudCheckResult(
    FraudRiskLevel riskLevel,
    String reason,
    double riskScore  // 0.0 (no risk) to 1.0 (maximum risk)
) {
    /**
     * Compact constructor - validation
     */
    public FraudCheckResult {
        if (riskLevel == null) {
            throw new IllegalArgumentException("Risk level cannot be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null or blank");
        }
        if (riskScore < 0.0 || riskScore > 1.0) {
            throw new IllegalArgumentException("Risk score must be between 0.0 and 1.0");
        }
    }
    
    /**
     * Check if this result indicates high risk
     * @return true if risk level is HIGH
     */
    public boolean isHighRisk() {
        return riskLevel == FraudRiskLevel.HIGH;
    }
    
    /**
     * Check if this result indicates safe transaction
     * @return true if risk level is LOW
     */
    public boolean isSafe() {
        return riskLevel == FraudRiskLevel.LOW;
    }
    
    /**
     * Check if manual review is required
     * @return true if risk level is MEDIUM or HIGH
     */
    public boolean requiresReview() {
        return riskLevel == FraudRiskLevel.MEDIUM || riskLevel == FraudRiskLevel.HIGH;
    }
}
