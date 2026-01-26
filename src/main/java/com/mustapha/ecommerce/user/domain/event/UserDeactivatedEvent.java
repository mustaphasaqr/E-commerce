package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event: User account has been deactivated
 * 
 * Critical for:
 * - Analytics (user churn analysis)
 * - Notification systems (goodbye emails, win-back campaigns)
 * - License management (free up seats in subscription models)
 * - Compliance (track voluntary account suspensions)
 * 
 * Raised when: User status transitions ACTIVE → INACTIVE
 */
public record UserDeactivatedEvent(
    UserId userId,
    Instant occurredAt
) implements DomainEvent {
    
    public UserDeactivatedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public UserDeactivatedEvent(UserId userId) {
        this(userId, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
