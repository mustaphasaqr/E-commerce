package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when a new user is created.
 * Immutable fact that has already happened.
 */
public record UserCreatedEvent(
    UserId userId,
    Username username,
    Email email,
    Instant occurredAt
) implements DomainEvent {
    
    public UserCreatedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public UserCreatedEvent(UserId userId, Username username, Email email) {
        this(userId, username, email, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
