package com.mustapha.ecommerce.cart.application.command;

import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;

/**
 * Convert Cart Command
 * 
 * Responsibility: Transfer data for converting cart to order
 * Pattern: Command (CQS - Command Query Separation)
 */
public class ConvertCartCommand {
    
    private final CartId cartId;
    private final Long orderId; // Reference to order context (primitive is OK)
    
    public ConvertCartCommand(CartId cartId, Long orderId) {
        if (cartId == null) {
            throw new IllegalArgumentException("Cart ID cannot be null");
        }
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        
        this.cartId = cartId;
        this.orderId = orderId;
    }
    
    public CartId getCartId() {
        return cartId;
    }
    
    public Long getOrderId() {
        return orderId;
    }
}
