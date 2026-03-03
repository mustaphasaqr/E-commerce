package com.mustapha.ecommerce.analytics.domain.model;

/**
 * Low Stock Product Value Object
 * Represents products with inventory below threshold
 */
public class LowStockProduct {
    private final String productId;
    private final String productName;
    private final int currentStock;
    private final int stockThreshold;
    private final long totalSold;

    public LowStockProduct(String productId, String productName, int currentStock, 
                          int stockThreshold, long totalSold) {
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.stockThreshold = stockThreshold;
        this.totalSold = totalSold;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getStockThreshold() {
        return stockThreshold;
    }

    public long getTotalSold() {
        return totalSold;
    }

    public boolean isCritical() {
        return currentStock == 0;
    }
}
