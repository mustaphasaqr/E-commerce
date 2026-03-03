package com.mustapha.ecommerce.cart.domain.event;

import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;

/**
 * Cart Converted Event
 * 
 * Emitted when a cart is successfully converted to an order.
 * 
 * Use cases:
 * - Conversion rate analytics
 * - Cart abandonment tracking (successful conversion)
 * - Revenue attribution
 */
public class CartConvertedEvent extends CartDomainEvent {
    
    private final Long orderId; // Reference to order context (primitive is OK)
    private final Money totalAmount;
    private final int itemCount;
    
    public CartConvertedEvent(CartId cartId, Long orderId, Money totalAmount, int itemCount) {
        super(cartId);
        
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("Total amount cannot be null");
        }
        
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public Money getTotalAmount() {
        return totalAmount;
    }
    
    public int getItemCount() {
        return itemCount;
    }
    
    @Override
    public String getEventType() {
        return "CART_CONVERTED";
    }
}
