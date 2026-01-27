package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Delete User Command (Soft Delete)
 * Responsibility: Transfer user ID and deletion reason
 */
public class DeleteUserCommand {
    
    private final UserId userId;
    private final String reason;
    
    public DeleteUserCommand(UserId userId, String reason) {
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
