package com.mustapha.ecommerce.product.application.command;

/**
 * Query: Get Product By SKU
 * 
 * Purpose: Retrieve product by its external identifier (SKU)
 * 
 * Validation:
 * - SKU cannot be null or empty
 * 
 * Pattern: Query (immutable)
 * Note: Separate from GetProductByIdQuery - different semantics (external vs internal ID)
 */
public record GetProductBySkuQuery(
    String sku
) {
    public GetProductBySkuQuery {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
    }
}
