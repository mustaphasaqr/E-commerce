package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Shipping Performance Domain Model
 * 
 * Represents delivery performance metrics for a carrier
 * Measures efficiency from order creation through delivery
 */
public class ShippingPerformance {
    private final String carrier;
    private final long totalShipments;
    private final long deliveredCount;
    private final double averageTimeToShipHours;
    private final double averageDeliveryTimeHours;
    private final double deliverySuccessRate;

    public ShippingPerformance(String carrier, 
                              long totalShipments,
                              long deliveredCount,
                              double averageTimeToShipHours,
                              double averageDeliveryTimeHours) {
        this.carrier = carrier;
        this.totalShipments = totalShipments;
        this.deliveredCount = deliveredCount;
        this.averageTimeToShipHours = averageTimeToShipHours;
        this.averageDeliveryTimeHours = averageDeliveryTimeHours;
        this.deliverySuccessRate = calculateDeliveryRate(deliveredCount, totalShipments);
    }

    private static double calculateDeliveryRate(long delivered, long total) {
        if (total == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(delivered)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
    }

    public String getCarrier() {
        return carrier;
    }

    public long getTotalShipments() {
        return totalShipments;
    }

    public long getDeliveredCount() {
        return deliveredCount;
    }

    public double getAverageTimeToShipHours() {
        return averageTimeToShipHours;
    }

    public double getAverageDeliveryTimeHours() {
        return averageDeliveryTimeHours;
    }

    public double getDeliverySuccessRate() {
        return deliverySuccessRate;
    }
    
    /**
     * Check if carrier performance is good (>95% delivery rate, <48h delivery)
     */
    public boolean isPerformanceGood() {
        return deliverySuccessRate >= 95.0 && averageDeliveryTimeHours <= 48.0;
    }
}
