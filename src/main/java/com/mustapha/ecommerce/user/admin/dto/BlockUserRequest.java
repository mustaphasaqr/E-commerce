package com.mustapha.ecommerce.user.admin.dto;

/**
 * Block User Request
 * Purpose: Request to block a user account (admin only)
 */
public record BlockUserRequest(
    String userId,
    String reason
) {}
