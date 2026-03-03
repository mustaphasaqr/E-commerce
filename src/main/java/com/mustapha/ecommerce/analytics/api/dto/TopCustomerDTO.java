package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Top Customer DTO
 * API representation of top customer statistics
 */
public record TopCustomerDTO(
    String customerId,
    String customerName,
    String customerEmail,
    Long totalOrders,
    BigDecimal totalSpent,
    BigDecimal averageOrderValue
) {}
