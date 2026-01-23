package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to modify discontinued product
 * 
 * Business Context: Discontinued is a terminal state - no modifications or reactivation allowed
 */
public final class ProductDiscontinuedException extends RuntimeException {
    private final String productId;

    public ProductDiscontinuedException(String productId) {
        super("Product is discontinued and cannot be modified: " + productId);
        this.productId = productId;
    }

    public ProductDiscontinuedException(String productId, Throwable cause) {
        super("Product is discontinued and cannot be modified: " + productId, cause);
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}