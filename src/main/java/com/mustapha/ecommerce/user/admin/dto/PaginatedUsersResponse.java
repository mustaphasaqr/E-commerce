package com.mustapha.ecommerce.user.admin.dto;

import com.mustapha.ecommerce.user.dto.UserListResponse;
import java.util.List;

/**
 * Paginated Users Response - Lightweight for list operations
 * Purpose: Wrapper for paginated user list (admin only)
 * Performance: Uses UserListResponse (67% smaller than UserResponse)
 */
public record PaginatedUsersResponse(
    List<UserListResponse> users,
    int currentPage,
    int pageSize,
    int totalPages,
    long totalElements
) {}
