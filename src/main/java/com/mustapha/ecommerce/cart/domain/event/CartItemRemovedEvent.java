package com.mustapha.ecommerce.cart.domain.event;

import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;

/**
 * Cart Item Removed Event
 * 
 * Emitted when a product is removed from the cart.
 * 
 * Use cases:
 * - Cart abandonment analysis (removal patterns)
 * - User behavior tracking
 * - Analytics - conversion funnel dropoff
 */
public class CartItemRemovedEvent extends CartDomainEvent {
    
    private final ProductId productId;
    private final String productName;
    
    public CartItemRemovedEvent(CartId cartId, ProductId productId, String productName) {
        super(cartId);
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        this.productId = productId;
        this.productName = productName;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    @Override
    public String getEventType() {
        return "CART_ITEM_REMOVED";
    }
}
