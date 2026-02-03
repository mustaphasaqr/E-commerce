package com.mustapha.ecommerce.user.admin.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Role;

public class ChangeUserRoleCommand {
    private final String userId;
    private final Role newRole;
    private final String changedBy;

    public ChangeUserRoleCommand(String userId, Role newRole, String changedBy) {
        this.userId = userId;
        this.newRole = newRole;
        this.changedBy = changedBy;
    }

    public String getUserId() {
        return userId;
    }

    public Role getNewRole() {
        return newRole;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
