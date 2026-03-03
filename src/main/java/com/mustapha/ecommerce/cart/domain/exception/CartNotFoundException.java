package com.mustapha.ecommerce.cart.domain.exception;

/**
 * Cart Not Found Exception
 * 
 * Domain Exception - thrown when a cart cannot be found.
 * 
 * Layer: DOMAIN (pure business logic)
 * HTTP Mapping: 404 Not Found (done by exception handler)
 */
public class CartNotFoundException extends RuntimeException {
    
    public CartNotFoundException(String message) {
        super(message);
    }
    
    public CartNotFoundException(Long cartId) {
        super("Cart not found with ID: " + cartId);
    }
}
