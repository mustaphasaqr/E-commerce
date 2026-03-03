package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Payment Method Statistics DTO
 * API representation of payment method stats
 */
public record PaymentMethodStatsDTO(
    String paymentMethod,
    Long transactionCount,
    BigDecimal totalAmount,
    Long successfulCount,
    Long failedCount,
    Double successRate,
    Double failureRate
) {}
