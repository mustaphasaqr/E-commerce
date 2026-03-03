package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;

/**
 * Payment Method Statistics Value Object
 * Represents payment method distribution and stats
 */
public class PaymentMethodStats {
    private final String paymentMethod;
    private final long transactionCount;
    private final BigDecimal totalAmount;
    private final long successfulCount;
    private final long failedCount;
    private final double successRate;

    public PaymentMethodStats(String paymentMethod, long transactionCount, BigDecimal totalAmount,
                             long successfulCount, long failedCount) {
        this.paymentMethod = paymentMethod;
        this.transactionCount = transactionCount;
        this.totalAmount = totalAmount;
        this.successfulCount = successfulCount;
        this.failedCount = failedCount;
        this.successRate = transactionCount > 0 
            ? (double) successfulCount / transactionCount * 100.0 
            : 0.0;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public long getSuccessfulCount() {
        return successfulCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getFailureRate() {
        return 100.0 - successRate;
    }
}
