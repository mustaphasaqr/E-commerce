package com.mustapha.ecommerce.cart.application.command;

import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

/**
 * Remove From Cart Command
 * 
 * Responsibility: Transfer data for removing item from cart
 * Pattern: Command (CQS - Command Query Separation)
 */
public class RemoveFromCartCommand {
    
    private final UserId userId;
    private final SessionId sessionId;
    private final ProductId productId;
    
    public RemoveFromCartCommand(UserId userId, SessionId sessionId, ProductId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        this.userId = userId;
        this.sessionId = sessionId;
        this.productId = productId;
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
}
