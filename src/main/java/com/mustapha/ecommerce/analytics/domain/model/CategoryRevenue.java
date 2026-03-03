package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;

/**
 * Category Revenue Value Object
 * Represents revenue breakdown by product category
 */
public class CategoryRevenue {
    private final String category;
    private final long productCount;
    private final long unitsSold;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageProductRevenue;

    public CategoryRevenue(String category, long productCount, long unitsSold, BigDecimal totalRevenue) {
        this.category = category;
        this.productCount = productCount;
        this.unitsSold = unitsSold;
        this.totalRevenue = totalRevenue;
        this.averageProductRevenue = productCount > 0
            ? totalRevenue.divide(BigDecimal.valueOf(productCount), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    public String getCategory() {
        return category;
    }

    public long getProductCount() {
        return productCount;
    }

    public long getUnitsSold() {
        return unitsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getAverageProductRevenue() {
        return averageProductRevenue;
    }
}
