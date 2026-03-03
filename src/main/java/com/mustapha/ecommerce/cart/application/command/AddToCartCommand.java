package com.mustapha.ecommerce.cart.application.command;

import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

/**
 * Add To Cart Command
 * 
 * Responsibility: Transfer data from Facade to Use Case
 * Pattern: Command (CQS - Command Query Separation)
 * 
 * Uses value objects for type safety:
 * - UserId (can be null for anonymous users)
 * - SessionId (can be null for authenticated users)
 * - ProductId (type-safe product identifier)
 */
public class AddToCartCommand {
    
    private final UserId userId;
    private final SessionId sessionId;
    private final ProductId productId;
    private final int quantity;
    
    public AddToCartCommand(UserId userId, SessionId sessionId, ProductId productId, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
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
