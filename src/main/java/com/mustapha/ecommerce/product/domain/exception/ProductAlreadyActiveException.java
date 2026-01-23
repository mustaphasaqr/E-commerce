package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to activate an already active product
 * 
 * Business Context: Prevents redundant state transitions (idempotency violation)
 */
public final class ProductAlreadyActiveException extends RuntimeException {
    private final String productId;

    public ProductAlreadyActiveException(String productId) {
        super("Product is already active: " + productId);
        this.productId = productId;
    }

    public ProductAlreadyActiveException(String productId, Throwable cause) {
        super("Product is already active: " + productId, cause);
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
