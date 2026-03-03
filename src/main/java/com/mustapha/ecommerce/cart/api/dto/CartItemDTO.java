package com.mustapha.ecommerce.cart.api.dto;

import java.math.BigDecimal;

/**
 * DTO for cart item
 */
public record CartItemDTO(
    Long productId,
    String productName,
    int quantity,
    BigDecimal price,
    BigDecimal subtotal
) {}
