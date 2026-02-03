package com.mustapha.ecommerce.user.admin.dto;

import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
    @NotNull(message = "New role is required")
    Role newRole,
    
    String reason
) {
}
