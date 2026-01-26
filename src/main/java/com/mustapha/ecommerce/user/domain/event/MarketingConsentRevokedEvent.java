package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when user revokes marketing consent.
 * Critical for:
 * - GDPR compliance (right to withdraw consent)
 * - Legal audit trail
 * - Immediate marketing campaign removal
 * - Analytics tracking
 */
public record MarketingConsentRevokedEvent(
    UserId userId,
    Instant occurredAt
) implements DomainEvent {
    
    public MarketingConsentRevokedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public MarketingConsentRevokedEvent(UserId userId) {
        this(userId, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
