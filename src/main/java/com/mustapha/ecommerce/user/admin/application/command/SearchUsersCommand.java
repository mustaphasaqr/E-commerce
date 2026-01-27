package com.mustapha.ecommerce.user.admin.application.command;

import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import org.springframework.data.domain.Pageable;

/**
 * Search Users Command
 * Purpose: Command for searching users by criteria with pagination (admin only)
 */
public record SearchUsersCommand(
    String email,
    String username,
    User.UserStatus status,
    Role role,
    Pageable pageable
) {}
