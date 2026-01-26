package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a user's username is changed.
 */
public record UsernameChangedEvent(
    UserId userId,
    Username oldUsername,
    Username newUsername,
    Instant occurredAt
) implements DomainEvent {
    
    public UsernameChangedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(oldUsername, "Old username cannot be null");
        Objects.requireNonNull(newUsername, "New username cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public UsernameChangedEvent(UserId userId, Username oldUsername, Username newUsername) {
        this(userId, oldUsername, newUsername, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
