package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Email;

/**
 * Password Reset Request Command
 * Responsibility: Transfer email for password reset initiation
 */
public class PasswordResetRequestCommand {
    
    private final Email email;
    
    public PasswordResetRequestCommand(Email email) {
        this.email = email;
    }
    
    public Email getEmail() {
        return email;
    }
}
