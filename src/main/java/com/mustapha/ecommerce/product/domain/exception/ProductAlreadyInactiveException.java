package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to deactivate an already inactive product
 */
public class ProductAlreadyInactiveException extends RuntimeException {
    public ProductAlreadyInactiveException(String productId) {
        super("Product is already inactive: " + productId);
    }
}
