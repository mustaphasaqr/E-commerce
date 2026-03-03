package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;

/**
 * Product Performance Value Object
 * Represents sales performance metrics for a product
 */
public class ProductPerformance {
    private final String productId;
    private final String productName;
    private final long unitsSold;
    private final BigDecimal totalRevenue;
    private final long orderCount;

    public ProductPerformance(
            String productId,
            String productName,
            long unitsSold,
            BigDecimal totalRevenue,
            long orderCount) {
        this.productId = productId;
        this.productName = productName;
        this.unitsSold = unitsSold;
        this.totalRevenue = totalRevenue;
        this.orderCount = orderCount;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public long getUnitsSold() {
        return unitsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getOrderCount() {
        return orderCount;
    }
}
