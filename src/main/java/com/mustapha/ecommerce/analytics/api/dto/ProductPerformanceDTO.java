package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * DTO for Product Performance Response
 */
public record ProductPerformanceDTO(
    String productId,
    String productName,
    long unitsSold,
    BigDecimal totalRevenue,
    long orderCount
) {
}
