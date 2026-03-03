package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Refund Statistics Domain Model
 * 
 * Represents refund metrics for a given period
 * 
 * Business Rules:
 * - Refund Rate = (Refunded Orders / Total Completed Orders) * 100
 * - Average Refund = Total Refund Amount / Refunded Orders
 * - High refund rate (>5%) may indicate quality issues
 */
public class RefundStats {
    private final long totalCompletedOrders;
    private final long refundRequestedCount;
    private final long refundApprovedCount;
    private final long refundCompletedCount;
    private final long refundRejectedCount;
    private final BigDecimal totalRefundAmount;
    private final BigDecimal refundRate;
    private final BigDecimal averageRefundAmount;

    public RefundStats(long totalCompletedOrders, 
                      long refundRequestedCount,
                      long refundApprovedCount,
                      long refundCompletedCount,
                      long refundRejectedCount,
                      BigDecimal totalRefundAmount) {
        this.totalCompletedOrders = totalCompletedOrders;
        this.refundRequestedCount = refundRequestedCount;
        this.refundApprovedCount = refundApprovedCount;
        this.refundCompletedCount = refundCompletedCount;
        this.refundRejectedCount = refundRejectedCount;
        this.totalRefundAmount = totalRefundAmount != null ? totalRefundAmount : BigDecimal.ZERO;
        this.refundRate = calculateRefundRate(totalCompletedOrders, refundCompletedCount);
        this.averageRefundAmount = calculateAverageRefund(this.totalRefundAmount, refundCompletedCount);
    }

    /**
     * Calculate refund rate as percentage of completed orders
     */
    private static BigDecimal calculateRefundRate(long totalOrders, long refundedOrders) {
        if (totalOrders == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(refundedOrders)
                        .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate average refund amount
     */
    private static BigDecimal calculateAverageRefund(BigDecimal totalAmount, long refundCount) {
        if (refundCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.divide(BigDecimal.valueOf(refundCount), 2, RoundingMode.HALF_UP);
    }

    public long getTotalCompletedOrders() {
        return totalCompletedOrders;
    }

    public long getRefundRequestedCount() {
        return refundRequestedCount;
    }

    public long getRefundApprovedCount() {
        return refundApprovedCount;
    }

    public long getRefundCompletedCount() {
        return refundCompletedCount;
    }

    public long getRefundRejectedCount() {
        return refundRejectedCount;
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public BigDecimal getRefundRate() {
        return refundRate;
    }

    public BigDecimal getAverageRefundAmount() {
        return averageRefundAmount;
    }
    
    /**
     * Check if refund rate is concerning (>5%)
     */
    public boolean isRefundRateHigh() {
        return refundRate.compareTo(BigDecimal.valueOf(5.0)) > 0;
    }
}
