package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when product is not found
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product not found with ID: " + productId);
    }

    public ProductNotFoundException(String field, String value) {
        super("Product not found with " + field + ": " + value);
    }
}
