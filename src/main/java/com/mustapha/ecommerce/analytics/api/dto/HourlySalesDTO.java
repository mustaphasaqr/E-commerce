package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Hourly Sales DTO
 * API representation of hourly sales statistics
 */
public record HourlySalesDTO(
    Integer hour,
    Long orderCount,
    BigDecimal revenue
) {}
