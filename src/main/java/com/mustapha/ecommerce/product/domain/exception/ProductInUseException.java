package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to modify a product that is currently in use by active orders
 */
public class ProductInUseException extends RuntimeException {
    public ProductInUseException(String message) {
        super(message);
    }
}
