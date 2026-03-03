package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Category Revenue DTO
 * API representation of revenue by category
 */
public record CategoryRevenueDTO(
    String category,
    Long productCount,
    Long unitsSold,
    BigDecimal totalRevenue,
    BigDecimal averageProductRevenue
) {}
