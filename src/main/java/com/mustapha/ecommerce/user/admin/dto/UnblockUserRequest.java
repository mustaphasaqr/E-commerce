package com.mustapha.ecommerce.user.admin.dto;

/**
 * Unblock User Request
 * Purpose: Request to unblock a user account (admin only)
 */
public record UnblockUserRequest(
    String reason  // Optional: reason for unblocking (audit trail)
) {}
