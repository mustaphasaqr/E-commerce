package com.mustapha.ecommerce.order.domain.exception;

/**
 * Invalid Order Item Exception
 * 
 * Domain Exception - thrown when order item validation fails.
 * 
 * Examples:
 * - Item quantity <= 0
 * - Item price <= 0
 * - Null item
 * - Invalid product reference
 * 
 * Business Rule: Every order item must be valid (positive quantity, positive price, valid product)
 * 
 * Layer: DOMAIN (pure business logic)
 * HTTP Mapping: 400 Bad Request (done by controller, not here)
 */
public class InvalidOrderItemException extends RuntimeException {
    
    public InvalidOrderItemException(String message) {
        super(message);
    }
    
    public InvalidOrderItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
