package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cart Abandonment Domain Model
 * 
 * Represents shopping cart abandonment statistics
 * Critical for understanding conversion funnel drop-off
 * 
 * Business Rules:
 * - Abandonment Rate = (Abandoned Carts / Total Carts) * 100
 * - Industry average abandonment rate: 69.8%
 * - High abandonment (>80%) indicates checkout friction
 */
public class CartAbandonment {
    private final long totalCarts;
    private final long activeCarts;
    private final long convertedCarts;
    private final long abandonedCarts;
    private final BigDecimal totalAbandonedValue;
    private final BigDecimal averageAbandonedValue;
    private final double abandonmentRate;
    private final double conversionRate;

    public CartAbandonment(long totalCarts,
                          long activeCarts,
                          long convertedCarts,
                          long abandonedCarts,
                          BigDecimal totalAbandonedValue) {
        this.totalCarts = totalCarts;
        this.activeCarts = activeCarts;
        this.convertedCarts = convertedCarts;
        this.abandonedCarts = abandonedCarts;
        this.totalAbandonedValue = totalAbandonedValue != null ? totalAbandonedValue : BigDecimal.ZERO;
        this.averageAbandonedValue = calculateAverageAbandonedValue(this.totalAbandonedValue, abandonedCarts);
        this.abandonmentRate = calculateAbandonmentRate(abandonedCarts, totalCarts);
        this.conversionRate = calculateConversionRate(convertedCarts, totalCarts);
    }

    private static BigDecimal calculateAverageAbandonedValue(BigDecimal total, long count) {
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private static double calculateAbandonmentRate(long abandoned, long total) {
        if (total == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(abandoned)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
    }

    private static double calculateConversionRate(long converted, long total) {
        if (total == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(converted)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
    }

    public long getTotalCarts() {
        return totalCarts;
    }

    public long getActiveCarts() {
        return activeCarts;
    }

    public long getConvertedCarts() {
        return convertedCarts;
    }

    public long getAbandonedCarts() {
        return abandonedCarts;
    }

    public BigDecimal getTotalAbandonedValue() {
        return totalAbandonedValue;
    }

    public BigDecimal getAverageAbandonedValue() {
        return averageAbandonedValue;
    }

    public double getAbandonmentRate() {
        return abandonmentRate;
    }

    public double getConversionRate() {
        return conversionRate;
    }
    
    /**
     * Check if abandonment rate is concerning (>80%)
     */
    public boolean isAbandonmentRateHigh() {
        return abandonmentRate > 80.0;
    }
    
    /**
     * Estimate potential revenue recovery (assumes 10% recovery rate)
     */
    public BigDecimal getPotentialRecovery() {
        return totalAbandonedValue.multiply(BigDecimal.valueOf(0.10))
                                  .setScale(2, RoundingMode.HALF_UP);
    }
}
