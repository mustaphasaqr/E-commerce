package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to deactivate an already inactive product
 * 
 * Business Context: Prevents redundant state transitions (idempotency violation)
 */
public final class ProductAlreadyInactiveException extends RuntimeException {
    private final String productId;

    public ProductAlreadyInactiveException(String productId) {
        super("Product is already inactive: " + productId);
        this.productId = productId;
    }

    public ProductAlreadyInactiveException(String productId, Throwable cause) {
        super("Product is already inactive: " + productId, cause);
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
