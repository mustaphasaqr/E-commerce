package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;

/**
 * Geographic Sales Domain Model
 * 
 * Represents sales aggregated by geographic location
 * Useful for identifying high-performing regions
 */
public class GeographicSales {
    private final String city;
    private final String state;
    private final String country;
    private final long orderCount;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageOrderValue;

    public GeographicSales(String city, String state, String country, 
                          long orderCount, BigDecimal totalRevenue) {
        this.city = city;
        this.state = state;
        this.country = country;
        this.orderCount = orderCount;
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = calculateAverageOrderValue(totalRevenue, orderCount);
    }

    private static BigDecimal calculateAverageOrderValue(BigDecimal totalRevenue, long orderCount) {
        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP);
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public String getLocationKey() {
        if (city != null && state != null) {
            return city + ", " + state + ", " + country;
        } else if (state != null) {
            return state + ", " + country;
        } else {
            return country;
        }
    }
}
