package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a user is blocked.
 */
public record UserBlockedEvent(
    UserId userId,
    String reason,
    Instant occurredAt
) implements DomainEvent {
    
    public UserBlockedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(reason, "Reason cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public UserBlockedEvent(UserId userId, String reason) {
        this(userId, reason, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
