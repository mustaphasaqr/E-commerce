package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Refund Statistics Data Transfer Object
 * API Layer representation
 */
public record RefundStatsDTO(
    long totalCompletedOrders,
    long refundRequestedCount,
    long refundApprovedCount,
    long refundCompletedCount,
    long refundRejectedCount,
    BigDecimal totalRefundAmount,
    BigDecimal refundRate,
    BigDecimal averageRefundAmount,
    boolean isRefundRateHigh
) {
}
