package com.mustapha.ecommerce.cart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to add item to cart
 */
public record AddToCartRequest(
    @NotBlank(message = "Product ID is required")
    String productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}
