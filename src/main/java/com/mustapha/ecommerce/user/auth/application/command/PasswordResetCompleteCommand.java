package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Password;

/**
 * Password Reset Complete Command
 * Responsibility: Transfer token and new password for password reset completion
 */
public class PasswordResetCompleteCommand {
    
    private final String token;
    private final Password newPassword;
    
    public PasswordResetCompleteCommand(String token, Password newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }
    
    public String getToken() {
        return token;
    }
    
    public Password getNewPassword() {
        return newPassword;
    }
}
