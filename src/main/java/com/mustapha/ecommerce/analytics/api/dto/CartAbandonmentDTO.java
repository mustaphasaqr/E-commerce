package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Cart Abandonment Data Transfer Object
 * API Layer representation
 */
public record CartAbandonmentDTO(
    long totalCarts,
    long activeCarts,
    long convertedCarts,
    long abandonedCarts,
    BigDecimal totalAbandonedValue,
    BigDecimal averageAbandonedValue,
    double abandonmentRate,
    double conversionRate,
    boolean isAbandonmentRateHigh,
    BigDecimal potentialRecovery
) {
}
