package com.mustapha.ecommerce.cart.application.command;

import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

/**
 * Get Cart Query
 * 
 * Responsibility: Transfer data for retrieving cart
 * Pattern: Query (CQS - Command Query Separation)
 */
public class GetCartQuery {
    
    private final UserId userId;
    private final SessionId sessionId;
    
    public GetCartQuery(UserId userId, SessionId sessionId) {
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
