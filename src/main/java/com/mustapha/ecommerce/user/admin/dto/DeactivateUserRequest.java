package com.mustapha.ecommerce.user.admin.dto;

/**
 * Deactivate User Request
 * Purpose: Request to deactivate a user account (admin only - temporary suspension)
 */
public record DeactivateUserRequest(
    String reason  // Optional: reason for deactivation (audit trail)
) {}
