package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Activate User Command
 * Responsibility: Transfer user ID and optional activation note
 */
public class ActivateUserCommand {
    
    private final UserId userId;
    private final String activationNote;  // Optional note for manual activation
    
    public ActivateUserCommand(UserId userId, String activationNote) {
        this.userId = userId;
        this.activationNote = activationNote;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getActivationNote() {
        return activationNote;
    }
}
