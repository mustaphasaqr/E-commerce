package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily Sales Value Object
 * Represents aggregated sales data for a single day
 */
public class DailySales {
    private final LocalDate date;
    private final long orderCount;
    private final BigDecimal revenue;
    private final BigDecimal averageOrderValue;

    public DailySales(LocalDate date, long orderCount, BigDecimal revenue) {
        this.date = date;
        this.orderCount = orderCount;
        this.revenue = revenue;
        this.averageOrderValue = orderCount > 0 
            ? revenue.divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }
}
