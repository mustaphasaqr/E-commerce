package com.mustapha.ecommerce.cart.domain.exception;

/**
 * Invalid Cart State Exception
 * 
 * Domain Exception - thrown when a business rule regarding cart state is violated.
 * 
 * Examples:
 * - Attempting to modify a converted cart
 * - Attempting to modify an abandoned cart
 * - Attempting invalid state transitions
 * 
 * Layer: DOMAIN (pure business logic)
 * HTTP Mapping: 409 Conflict (done by exception handler)
 */
public class InvalidCartStateException extends RuntimeException {
    
    public InvalidCartStateException(String message) {
        super(message);
    }
    
    public InvalidCartStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
