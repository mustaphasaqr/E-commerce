package com.mustapha.ecommerce.user.domain.event;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event: User has accepted terms and conditions
 * 
 * Critical for:
 * - Legal compliance (proof of consent with timestamp)
 * - Audit trail (regulatory requirements - GDPR, CCPA, etc.)
 * - Version tracking (which terms version user agreed to)
 * - Analytics (terms acceptance rate, upgrade adoption)
 * 
 * Raised when: User accepts terms (new acceptance or version upgrade)
 * 
 * Important: Even with idempotency in acceptTerms(), this event is raised
 * only when actual state change occurs (new version or first acceptance)
 */
public record TermsAcceptedEvent(
    UserId userId,
    String termsVersion,
    Instant occurredAt
) implements DomainEvent {
    
    public TermsAcceptedEvent {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(termsVersion, "Terms version cannot be null");
        if (termsVersion.isBlank()) {
            throw new IllegalArgumentException("Terms version cannot be blank");
        }
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
    
    public TermsAcceptedEvent(UserId userId, String termsVersion) {
        this(userId, termsVersion, Instant.now());
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
