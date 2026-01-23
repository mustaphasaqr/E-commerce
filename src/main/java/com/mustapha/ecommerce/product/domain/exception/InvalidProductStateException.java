package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when product is in an invalid state for an operation
 * 
 * Business Context Examples:
 * - Cannot reserve stock for inactive product
 * - Cannot reserve stock for product not available for purchase
 * - Cannot deactivate product with reserved stock (active orders)
 * - Cannot modify discontinued product
 */
public final class InvalidProductStateException extends RuntimeException {
    public InvalidProductStateException(String message) {
        super(message);
    }

    public InvalidProductStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
