package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a user's email is verified.
 */
public record UserEmailVerifiedEvent(
    UserId userId,
    Instant occurredAt
) implements DomainEvent {
    
    public UserEmailVerifiedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public UserEmailVerifiedEvent(UserId userId) {
        this(userId, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
