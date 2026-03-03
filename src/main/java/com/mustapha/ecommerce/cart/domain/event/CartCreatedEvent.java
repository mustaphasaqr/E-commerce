package com.mustapha.ecommerce.cart.domain.event;

import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

/**
 * Cart Created Event
 * 
 * Emitted when a new cart is created for a user or session.
 * 
 * Use cases:
 * - Analytics tracking (cart creation rate)
 * - User behavior analysis
 * - Session tracking
 */
public class CartCreatedEvent extends CartDomainEvent {
    
    private final UserId userId; // Can be null for anonymous carts
    private final SessionId sessionId; // Can be null for authenticated user carts
    
    public CartCreatedEvent(CartId cartId, UserId userId, SessionId sessionId) {
        super(cartId);
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public SessionId getSessionId() {
        return sessionId;
    }
    
    @Override
    public String getEventType() {
        return "CART_CREATED";
    }
}
