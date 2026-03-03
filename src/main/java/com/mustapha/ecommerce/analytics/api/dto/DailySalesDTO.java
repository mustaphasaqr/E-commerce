package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Daily Sales Response
 */
public record DailySalesDTO(
    LocalDate date,
    long orderCount,
    BigDecimal revenue,
    BigDecimal averageOrderValue
) {
}
