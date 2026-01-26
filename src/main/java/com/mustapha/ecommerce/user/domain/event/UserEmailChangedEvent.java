package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a user's email is changed.
 */
public record UserEmailChangedEvent(
    UserId userId,
    Email oldEmail,
    Email newEmail,
    Instant occurredAt
) implements DomainEvent {
    
    public UserEmailChangedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(oldEmail, "Old email cannot be null");
        Objects.requireNonNull(newEmail, "New email cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public UserEmailChangedEvent(UserId userId, Email oldEmail, Email newEmail) {
        this(userId, oldEmail, newEmail, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
