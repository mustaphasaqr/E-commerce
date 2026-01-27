package com.mustapha.ecommerce.user.admin.dto;

import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;

/**
 * Search Users Request
 * Purpose: Criteria for searching users (admin only)
 */
public record SearchUsersRequest(
    String email,
    String username,
    User.UserStatus status,
    Role role,
    int page,
    int size
) {
    public SearchUsersRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100; // Max page size
    }
}
