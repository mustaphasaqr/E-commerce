package com.mustapha.ecommerce.cart.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for cart response
 */
public record CartDTO(
    Long id,
    String userId,
    String sessionId,
    List<CartItemDTO> items,
    BigDecimal totalAmount,
    String status,
    int totalItems
) {}
