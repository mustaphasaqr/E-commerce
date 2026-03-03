package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;

/**
 * Sales Summary Value Object
 * Represents overall sales statistics for a given period
 */
public class SalesSummary {
    private final long totalOrders;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageOrderValue;
    private final long completedOrders;
    private final long cancelledOrders;
    private final long pendingOrders;

    public SalesSummary(
            long totalOrders,
            BigDecimal totalRevenue,
            long completedOrders,
            long cancelledOrders,
            long pendingOrders) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = totalOrders > 0
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
        this.completedOrders = completedOrders;
        this.cancelledOrders = cancelledOrders;
        this.pendingOrders = pendingOrders;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    /**
     * Calculate completion rate as percentage
     * @return completion rate (0-100)
     */
    public double getCompletionRate() {
        return totalOrders > 0
            ? (double) completedOrders / totalOrders * 100.0
            : 0.0;
    }
}
