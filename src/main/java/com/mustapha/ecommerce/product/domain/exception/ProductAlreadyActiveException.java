package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to activate an already active product
 */
public class ProductAlreadyActiveException extends RuntimeException {
    public ProductAlreadyActiveException(String productId) {
        super("Product is already active: " + productId);
    }
}
