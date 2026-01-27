package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Logout All Devices Command
 * Responsibility: Transfer user ID for revoking all sessions and tokens
 */
public class LogoutAllDevicesCommand {
    
    private final UserId userId;
    private final String currentSessionId; // Keep current session active
    
    public LogoutAllDevicesCommand(UserId userId, String currentSessionId) {
        this.userId = userId;
        this.currentSessionId = currentSessionId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getCurrentSessionId() {
        return currentSessionId;
    }
}
