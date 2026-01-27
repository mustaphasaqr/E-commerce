package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Change Password Command
 * Responsibility: Transfer user ID, current password (plain text for verification), and new password (plain text to be hashed)
 */
public class ChangePasswordCommand {
    
    private final UserId userId;
    private final String currentPasswordPlainText;
    private final String newPasswordPlainText;
    
    public ChangePasswordCommand(UserId userId, String currentPasswordPlainText, String newPasswordPlainText) {
        this.userId = userId;
        this.currentPasswordPlainText = currentPasswordPlainText;
        this.newPasswordPlainText = newPasswordPlainText;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getCurrentPasswordPlainText() {
        return currentPasswordPlainText;
    }
    
    public String getNewPasswordPlainText() {
        return newPasswordPlainText;
    }
}
