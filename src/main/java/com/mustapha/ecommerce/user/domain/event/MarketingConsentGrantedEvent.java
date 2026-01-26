package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event raised when user grants marketing consent.
 * Critical for:
 * - Analytics tracking
 * - GDPR compliance audit logs
 * - Marketing campaign segmentation
 */
public record MarketingConsentGrantedEvent(
    UserId userId,
    Instant occurredAt
) implements DomainEvent {
    
    public MarketingConsentGrantedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public MarketingConsentGrantedEvent(UserId userId) {
        this(userId, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
