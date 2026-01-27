package com.mustapha.ecommerce.user.auth.domain.event;

import com.mustapha.ecommerce.user.domain.event.DomainEvent;

/**
 * Marker interface for all Auth domain events.
 * Extends DomainEvent to be compatible with DomainEventPublisher.
 */
public interface AuthDomainEvent extends DomainEvent {
}
