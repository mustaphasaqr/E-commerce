package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Profit Margin Domain Model
 * 
 * Represents profitability metrics for a product
 * Requires COGS (Cost of Goods Sold) data
 * 
 * Business Rules:
 * - Profit = Revenue - Cost
 * - Profit Margin % = (Profit / Revenue) * 100
 * - Negative profit = Loss
 */
public class ProfitMargin {
    private final String productId;
    private final String productName;
    private final long unitsSold;
    private final BigDecimal revenue;
    private final BigDecimal cost;
    private final BigDecimal profit;
    private final BigDecimal profitMarginPercent;

    public ProfitMargin(String productId, String productName, long unitsSold, 
                       BigDecimal revenue, BigDecimal cost) {
        this.productId = productId;
        this.productName = productName;
        this.unitsSold = unitsSold;
        this.revenue = revenue;
        this.cost = cost;
        this.profit = revenue.subtract(cost);
        this.profitMarginPercent = calculateProfitMarginPercent(revenue, profit);
    }

    /**
     * Calculate profit margin percentage
     * Returns 0 if revenue is 0 to avoid division by zero
     */
    private static BigDecimal calculateProfitMarginPercent(BigDecimal revenue, BigDecimal profit) {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return profit.divide(revenue, 4, RoundingMode.HALF_UP)
                     .multiply(BigDecimal.valueOf(100))
                     .setScale(2, RoundingMode.HALF_UP);
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

    public BigDecimal getRevenue() {
        return revenue;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public BigDecimal getProfitMarginPercent() {
        return profitMarginPercent;
    }
    
    public boolean isProfitable() {
        return profit.compareTo(BigDecimal.ZERO) > 0;
    }
}
