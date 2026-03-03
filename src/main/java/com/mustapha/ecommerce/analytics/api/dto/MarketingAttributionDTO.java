package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Marketing Attribution Data Transfer Object
 * API Layer representation
 */
public record MarketingAttributionDTO(
    String source,
    String campaign,
    long orderCount,
    BigDecimal totalRevenue,
    BigDecimal averageOrderValue,
    long customerCount,
    double conversionRate,
    String channelKey
) {
}
