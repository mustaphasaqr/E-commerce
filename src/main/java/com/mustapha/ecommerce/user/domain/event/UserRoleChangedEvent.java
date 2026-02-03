package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;

public record UserRoleChangedEvent(
    UserId userId,
    Role oldRole,
    Role newRole,
    String changedBy,
    Instant occurredAt
) implements DomainEvent {
    
    public UserRoleChangedEvent(UserId userId, Role oldRole, Role newRole, String changedBy) {
        this(userId, oldRole, newRole, changedBy, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
