package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Geographic Sales Data Transfer Object
 * API Layer representation
 */
public record GeographicSalesDTO(
    String city,
    String state,
    String country,
    long orderCount,
    BigDecimal totalRevenue,
    BigDecimal averageOrderValue,
    String locationKey
) {
}
