package com.mustapha.ecommerce.user.admin.dto;

import com.mustapha.ecommerce.user.dto.UserResponse;
import java.util.List;

/**
 * Paginated Users Response
 * Purpose: Wrapper for paginated user list (admin only)
 */
public record PaginatedUsersResponse(
    List<UserResponse> users,
    int currentPage,
    int pageSize,
    int totalPages,
    long totalElements
) {}
