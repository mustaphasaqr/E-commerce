package com.mustapha.ecommerce.cart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request to update cart item quantity
 */
public record UpdateCartItemRequest(
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative (0 to remove)")
    Integer quantity
) {}
