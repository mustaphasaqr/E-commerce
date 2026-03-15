package com.mustapha.ecommerce.user.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Delete User Request
 * Purpose: Request to delete a user account (admin only - soft delete for GDPR)
 */
public record DeleteUserRequest(
    @NotBlank(message = "Deletion reason is required")
    String reason
) {}
