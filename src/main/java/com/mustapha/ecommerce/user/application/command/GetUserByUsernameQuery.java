package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Username;

/**
 * Get User By Username Query
 * Responsibility: Transfer username for user retrieval
 * Pattern: Query (CQS - Command Query Separation)
 */
public class GetUserByUsernameQuery {
    
    private final Username username;
    
    public GetUserByUsernameQuery(Username username) {
        this.username = username;
    }
    
    public Username getUsername() {
        return username;
    }
}
