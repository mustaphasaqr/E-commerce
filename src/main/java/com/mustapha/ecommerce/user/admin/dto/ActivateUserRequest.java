package com.mustapha.ecommerce.user.admin.dto;

/**
 * Activate User Request
 * Purpose: Request to activate a user account (admin only - for manual activation)
 */
public record ActivateUserRequest(
    String activationNote  // Optional: note for why manual activation was needed
) {}
