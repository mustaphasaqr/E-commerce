package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when product is in an invalid state for an operation
 */
public class InvalidProductStateException extends RuntimeException {
    public InvalidProductStateException(String message) {
        super(message);
    }
}
