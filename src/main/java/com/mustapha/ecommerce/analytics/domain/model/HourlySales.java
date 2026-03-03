package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;

/**
 * Hourly Sales Value Object
 * Represents sales statistics by hour of day
 */
public class HourlySales {
    private final int hour;
    private final long orderCount;
    private final BigDecimal revenue;

    public HourlySales(int hour, long orderCount, BigDecimal revenue) {
        this.hour = hour;
        this.orderCount = orderCount;
        this.revenue = revenue;
    }

    public int getHour() {
        return hour;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
