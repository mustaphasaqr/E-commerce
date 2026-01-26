package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a user account is deleted (soft delete).
 */
public record UserDeletedEvent(
    UserId userId,
    String reason,
    Instant occurredAt
) implements DomainEvent {
    
    public UserDeletedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
        // reason can be null
    }
    
    public UserDeletedEvent(UserId userId, String reason) {
        this(userId, reason, Instant.now());
    }
    
    public UserDeletedEvent(UserId userId) {
        this(userId, null, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
