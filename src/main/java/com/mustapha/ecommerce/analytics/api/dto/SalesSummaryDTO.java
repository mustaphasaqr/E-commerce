package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * DTO for Sales Summary Response
 */
public record SalesSummaryDTO(
    long totalOrders,
    BigDecimal totalRevenue,
    BigDecimal averageOrderValue,
    long completedOrders,
    long cancelledOrders,
    long pendingOrders,
    double completionRate
) {
    public SalesSummaryDTO(
            long totalOrders,
            BigDecimal totalRevenue,
            BigDecimal averageOrderValue,
            long completedOrders,
            long cancelledOrders,
            long pendingOrders) {
        this(
            totalOrders,
            totalRevenue,
            averageOrderValue,
            completedOrders,
            cancelledOrders,
            pendingOrders,
            totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0.0
        );
    }
}
