package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Logout Command
 * Responsibility: Transfer session ID for logout
 */
public class LogoutCommand {
    
    private final UserId userId;
    private final String sessionId;
    
    public LogoutCommand(UserId userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
}
