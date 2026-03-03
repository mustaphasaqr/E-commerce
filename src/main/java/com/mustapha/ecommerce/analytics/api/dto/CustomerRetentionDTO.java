package com.mustapha.ecommerce.analytics.api.dto;

/**
 * Customer Retention DTO
 * API representation of customer retention metrics
 */
public record CustomerRetentionDTO(
    Long totalCustomers,
    Long returningCustomers,
    Long newCustomers,
    Double retentionRate,
    Double churnRate
) {}
