package com.mustapha.ecommerce.cart.domain.event;

import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;

import java.time.LocalDateTime;

/**
 * Base Cart Domain Event
 * 
 * Represents something that happened in the cart domain.
 * Domain events are facts - they represent things that have already occurred.
 * 
 * Pattern: Event Sourcing / Domain Events
 * Layer: DOMAIN
 */
public abstract class CartDomainEvent {
    
    private final CartId cartId;
    private final LocalDateTime occurredAt;
    
    protected CartDomainEvent(CartId cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("Cart ID cannot be null");
        }
        this.cartId = cartId;
        this.occurredAt = LocalDateTime.now();
    }
    
    public CartId getCartId() {
        return cartId;
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    public abstract String getEventType();
}
