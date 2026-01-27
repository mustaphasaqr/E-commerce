package com.mustapha.ecommerce.user.admin.application.command;

import org.springframework.data.domain.Pageable;

/**
 * Get All Users Command
 * Purpose: Command for retrieving paginated list of all users (admin only)
 */
public record GetAllUsersCommand(
    Pageable pageable
) {}
