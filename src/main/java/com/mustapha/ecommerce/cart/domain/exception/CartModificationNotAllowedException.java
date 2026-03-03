package com.mustapha.ecommerce.cart.domain.exception;

/**
 * Cart Modification Not Allowed Exception
 * 
 * Domain Exception - thrown when attempting to modify a cart that shouldn't be modified.
 * 
 * Examples:
 * - Modifying a cart that has already been converted to an order
 * - Modifying an abandoned cart
 * 
 * Layer: DOMAIN (pure business logic)
 * HTTP Mapping: 403 Forbidden (done by exception handler)
 */
public class CartModificationNotAllowedException extends RuntimeException {
    
    public CartModificationNotAllowedException(String message) {
        super(message);
    }
    
    public CartModificationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
