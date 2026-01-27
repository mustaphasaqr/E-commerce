package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Deactivate User Command
 * Responsibility: Transfer user ID and optional reason for deactivation
 */
public class DeactivateUserCommand {
    
    private final UserId userId;
    private final String reason;  // Optional reason for audit trail
    
    public DeactivateUserCommand(UserId userId, String reason) {
        this.userId = userId;
        this.reason = reason;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getReason() {
        return reason;
    }
}
