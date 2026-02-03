package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Logout Command
 * Responsibility: Transfer session ID and token for logout
 */
public class LogoutCommand {
    
    private final UserId userId;
    private final String sessionId;
    private final String token; // JWT token to blacklist
    
    public LogoutCommand(UserId userId, String sessionId, String token) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.token = token;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getToken() {
        return token;
    }
}
