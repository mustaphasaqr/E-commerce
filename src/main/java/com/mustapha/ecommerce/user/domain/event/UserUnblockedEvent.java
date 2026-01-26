package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event: User has been unblocked
 * 
 * Critical for:
 * - Support systems (ticket closure tracking)
 * - Risk engines (user rehabilitation tracking)
 * - Audit logs (compliance and legal)
 * - Analytics (user lifecycle analysis)
 * 
 * Raised when: User status transitions from BLOCKED → INACTIVE
 */
public record UserUnblockedEvent(
    UserId userId,
    Instant occurredAt
) implements DomainEvent {
    
    public UserUnblockedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public UserUnblockedEvent(UserId userId) {
        this(userId, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
