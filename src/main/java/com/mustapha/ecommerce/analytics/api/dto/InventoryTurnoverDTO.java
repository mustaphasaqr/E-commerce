package com.mustapha.ecommerce.analytics.api.dto;

/**
 * Inventory Turnover DTO
 * API representation of inventory turnover statistics
 */
public record InventoryTurnoverDTO(
    String productId,
    String productName,
    Long unitsSold,
    Integer averageStock,
    Double turnoverRate,
    Integer daysToSellOut
) {}
