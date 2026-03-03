package com.mustapha.ecommerce.cart.application.command;

import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

/**
 * Update Cart Item Command
 * 
 * Responsibility: Transfer data for updating cart item quantity
 * Pattern: Command (CQS - Command Query Separation)
 */
public class UpdateCartItemCommand {
    
    private final UserId userId;
    private final SessionId sessionId;
    private final ProductId productId;
    private final int quantity;
    
    public UpdateCartItemCommand(UserId userId, SessionId sessionId, ProductId productId, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        this.userId = userId;
        this.sessionId = sessionId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public SessionId getSessionId() {
        return sessionId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
}
