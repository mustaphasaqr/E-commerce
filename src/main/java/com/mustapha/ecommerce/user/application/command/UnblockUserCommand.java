package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Unblock User Command
 * Responsibility: Transfer user ID and optional reason for unblocking
 */
public class UnblockUserCommand {
    
    private final UserId userId;
    private final String reason;  // Optional reason for audit trail
    
    public UnblockUserCommand(UserId userId, String reason) {
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
