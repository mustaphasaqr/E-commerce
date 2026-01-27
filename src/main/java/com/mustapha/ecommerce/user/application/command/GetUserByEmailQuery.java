package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Email;

/**
 * Get User By Email Query
 * Responsibility: Transfer email for user retrieval
 * Pattern: Query (CQS - Command Query Separation)
 */
public class GetUserByEmailQuery {
    
    private final Email email;
    
    public GetUserByEmailQuery(Email email) {
        this.email = email;
    }
    
    public Email getEmail() {
        return email;
    }
}
