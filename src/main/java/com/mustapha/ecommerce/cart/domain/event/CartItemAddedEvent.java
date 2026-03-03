package com.mustapha.ecommerce.cart.domain.event;

import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;

/**
 * Cart Item Added Event
 * 
 * Emitted when a product is added to the cart.
 * 
 * Use cases:
 * - Product popularity tracking
 * - Real-time inventory monitoring
 * - Recommendation engine updates
 */
public class CartItemAddedEvent extends CartDomainEvent {
    
    private final ProductId productId;
    private final String productName;
    private final int quantity;
    private final Money price;
    
    public CartItemAddedEvent(CartId cartId, ProductId productId, String productName, int quantity, Money price) {
        super(cartId);
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public Money getPrice() {
        return price;
    }
    
    @Override
    public String getEventType() {
        return "CART_ITEM_ADDED";
    }
}
