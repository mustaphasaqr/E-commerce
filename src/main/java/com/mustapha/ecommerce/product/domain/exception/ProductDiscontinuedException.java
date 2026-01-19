package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to modify discontinued product
 */
public class ProductDiscontinuedException extends RuntimeException {
    public ProductDiscontinuedException(String productId) {
        super("Product is discontinued and cannot be modified: " + productId);
    }
}