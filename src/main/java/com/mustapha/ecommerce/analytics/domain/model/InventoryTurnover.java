package com.mustapha.ecommerce.analytics.domain.model;

/**
 * Inventory Turnover Value Object
 * Represents how fast products sell
 */
public class InventoryTurnover {
    private final String productId;
    private final String productName;
    private final long unitsSold;
    private final int averageStock;
    private final double turnoverRate;
    private final int daysToSellOut;

    public InventoryTurnover(String productId, String productName, long unitsSold, 
                            int averageStock, int periodDays) {
        this.productId = productId;
        this.productName = productName;
        this.unitsSold = unitsSold;
        this.averageStock = averageStock;
        this.turnoverRate = averageStock > 0 
            ? (double) unitsSold / averageStock 
            : 0.0;
        this.daysToSellOut = unitsSold > 0 && averageStock > 0
            ? (int) ((double) averageStock / unitsSold * periodDays)
            : 0;
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

    public int getAverageStock() {
        return averageStock;
    }

    public double getTurnoverRate() {
        return turnoverRate;
    }

    public int getDaysToSellOut() {
        return daysToSellOut;
    }
}
