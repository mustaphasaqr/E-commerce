package com.mustapha.ecommerce.analytics.api.dto;

/**
 * Low Stock Product DTO
 * API representation of low stock products
 */
public record LowStockProductDTO(
    String productId,
    String productName,
    Integer currentStock,
    Integer stockThreshold,
    Long totalSold,
    Boolean isCritical
) {}
