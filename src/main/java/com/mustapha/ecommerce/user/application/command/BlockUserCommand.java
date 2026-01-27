package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Block User Command
 * Responsibility: Transfer user ID and block reason
 */
public class BlockUserCommand {
    
    private final UserId userId;
    private final String reason;
    
    public BlockUserCommand(UserId userId, String reason) {
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
