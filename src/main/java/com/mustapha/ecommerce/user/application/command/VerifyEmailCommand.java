package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Verify Email Command
 * Responsibility: Transfer user ID for email verification
 */
public class VerifyEmailCommand {
    
    private final UserId userId;
    
    public VerifyEmailCommand(UserId userId) {
        this.userId = userId;
    }
    
    public UserId getUserId() {
        return userId;
    }
}
