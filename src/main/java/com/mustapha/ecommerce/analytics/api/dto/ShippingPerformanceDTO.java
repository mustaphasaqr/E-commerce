package com.mustapha.ecommerce.analytics.api.dto;

/**
 * Shipping Performance Data Transfer Object
 * API Layer representation
 */
public record ShippingPerformanceDTO(
    String carrier,
    long totalShipments,
    long deliveredCount,
    double averageTimeToShipHours,
    double averageDeliveryTimeHours,
    double deliverySuccessRate,
    boolean isPerformanceGood
) {
}
