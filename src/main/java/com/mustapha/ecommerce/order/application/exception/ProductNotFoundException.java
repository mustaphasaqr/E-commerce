package com.mustapha.ecommerce.order.application.exception;

import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Product Not Found Exception
 * Thrown when attempting to access a product that doesn't exist in the catalog
 * Layer: Application (external system failure, not domain rule violation)
 * 
 * This is NOT a domain exception because:
 * - Product catalog is external system concern (Infrastructure)
 * - Domain only validates order items, not product existence
 */
public final class ProductNotFoundException extends RuntimeException {
    
    private final ProductId productId;
    
    public ProductNotFoundException(ProductId productId) {
        super(String.format("Product not found: %s", productId.getValue()));
        this.productId = productId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
}
