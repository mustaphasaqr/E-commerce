package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Get User By ID Query
 * Responsibility: Transfer user ID for retrieval
 * Pattern: Query (CQS - Command Query Separation)
 */
public class GetUserByIdQuery {
    
    private final UserId userId;
    
    public GetUserByIdQuery(UserId userId) {
        this.userId = userId;
    }
    
    public UserId getUserId() {
        return userId;
    }
}
