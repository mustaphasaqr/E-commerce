package com.mustapha.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Change Email Request DTO - Complete API Contract
 * Responsibility: API contract for email change
 * 
 * Contains:
 * - New email address (validated)
 */
public class ChangeEmailRequest {
    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    private String newEmail;

    public ChangeEmailRequest() {
    }

    public ChangeEmailRequest(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}
