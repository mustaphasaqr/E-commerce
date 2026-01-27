package com.mustapha.ecommerce.user.admin.dto;

/**
 * Delete User Request
 * Purpose: Request to delete a user account (admin only - soft delete for GDPR)
 */
public record DeleteUserRequest(
    String userId,
    String reason
) {}
