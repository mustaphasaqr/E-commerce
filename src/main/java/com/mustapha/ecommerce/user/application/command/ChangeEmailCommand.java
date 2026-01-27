package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Change Email Command
 * Responsibility: Transfer user ID and new email
 */
public class ChangeEmailCommand {
    
    private final UserId userId;
    private final Email newEmail;
    
    public ChangeEmailCommand(UserId userId, Email newEmail) {
        this.userId = userId;
        this.newEmail = newEmail;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public Email getNewEmail() {
        return newEmail;
    }
}
