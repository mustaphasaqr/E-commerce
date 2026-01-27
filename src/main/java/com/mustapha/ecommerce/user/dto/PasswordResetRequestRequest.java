package com.mustapha.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Password Reset Request DTO - Complete API Contract
 * Responsibility: API contract for password reset initiation
 * 
 * Contains:
 * - Email (user account identifier)
 */
public class PasswordResetRequestRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    public PasswordResetRequestRequest() {
    }

    public PasswordResetRequestRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
