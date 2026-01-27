package com.mustapha.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Password Reset Complete Request DTO - Complete API Contract
 * Responsibility: API contract for completing password reset
 * 
 * Contains:
 * - Reset token (from email link)
 * - New password (validated strength)
 */
public class PasswordResetCompleteRequest {
    @NotBlank(message = "Reset token is required")
    private String token;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String newPassword;

    public PasswordResetCompleteRequest() {
    }

    public PasswordResetCompleteRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
