package com.mustapha.ecommerce.cart.domain.exception;

/**
 * Invalid Cart Item Exception
 * 
 * Domain Exception - thrown when cart item validation fails.
 * 
 * Examples:
 * - Quantity is zero or negative
 * - Price is negative
 * - Product ID is null
 * 
 * Layer: DOMAIN (pure business logic)
 * HTTP Mapping: 400 Bad Request (done by exception handler)
 */
public class InvalidCartItemException extends RuntimeException {
    
    public InvalidCartItemException(String message) {
        super(message);
    }
    
    public InvalidCartItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
