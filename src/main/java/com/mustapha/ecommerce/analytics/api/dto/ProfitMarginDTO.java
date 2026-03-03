package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Profit Margin Data Transfer Object
 * API Layer representation
 */
public record ProfitMarginDTO(
    String productId,
    String productName,
    long unitsSold,
    BigDecimal revenue,
    BigDecimal cost,
    BigDecimal profit,
    BigDecimal profitMarginPercent,
    boolean isProfitable
) {
}
