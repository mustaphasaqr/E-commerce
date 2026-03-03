package com.mustapha.ecommerce.cart.domain.event;

import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

/**
 * Cart Abandoned Event
 * 
 * Emitted when a cart is marked as abandoned (idle for > 24 hours).
 * 
 * Use cases:
 * - Cart abandonment analytics
 * - Re-engagement campaigns (email reminders)
 * - Lost revenue tracking
 */
public class CartAbandonedEvent extends CartDomainEvent {
    
    private final Money abandonedValue;
    private final int itemCount;
    private final UserId userId; // Can be null for anonymous carts
    
    public CartAbandonedEvent(CartId cartId, Money abandonedValue, int itemCount, UserId userId) {
        super(cartId);
        
        if (abandonedValue == null) {
            throw new IllegalArgumentException("Abandoned value cannot be null");
        }
        
        this.abandonedValue = abandonedValue;
        this.itemCount = itemCount;
        this.userId = userId;
    }
    
    public Money getAbandonedValue() {
        return abandonedValue;
    }
    
    public int getItemCount() {
        return itemCount;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    @Override
    public String getEventType() {
        return "CART_ABANDONED";
    }
}
