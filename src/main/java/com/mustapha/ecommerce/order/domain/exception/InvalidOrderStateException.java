package com.mustapha.ecommerce.order.domain.exception;

/**
 * Invalid Order State Exception
 * 
 * Domain Exception - thrown when a business rule regarding order state transitions is violated.
 * 
 * Examples:
 * - Attempting to pay an order that is already paid
 * - Attempting to ship an order that is not yet paid
 * - Attempting to transition to an invalid state
 * 
 * Layer: DOMAIN (pure business logic)
 * HTTP Mapping: 409 Conflict (done by controller, not here)
 */
public class InvalidOrderStateException extends RuntimeException {
    
    public InvalidOrderStateException(String message) {
        super(message);
    }
    
    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
