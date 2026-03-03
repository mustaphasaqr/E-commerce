package com.mustapha.ecommerce.cart.application.command;

import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

/**
 * Clear Cart Command
 * 
 * Responsibility: Transfer data for clearing entire cart
 * Pattern: Command (CQS - Command Query Separation)
 */
public class ClearCartCommand {
    
    private final UserId userId;
    private final SessionId sessionId;
    
    public ClearCartCommand(UserId userId, SessionId sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public SessionId getSessionId() {
        return sessionId;
    }
}
