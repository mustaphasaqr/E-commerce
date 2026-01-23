package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Query: Get Product By ID
 * 
 * Purpose: Retrieve product by its internal identifier
 * 
 * Validation:
 * - Product ID cannot be null
 * 
 * Pattern: Query (immutable)
 * Note: Separate from GetProductBySkuQuery for clear semantics
 * Uses ProductId value object (matches OrderId in GetOrderQuery)
 */
public class GetProductByIdQuery {
    
    private final ProductId productId;
    
    public GetProductByIdQuery(ProductId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.productId = productId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
}
