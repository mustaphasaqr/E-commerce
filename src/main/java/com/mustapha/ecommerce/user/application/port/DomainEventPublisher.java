package com.mustapha.ecommerce.user.application.port;

import com.mustapha.ecommerce.user.domain.event.DomainEvent;

/**
 * Domain Event Publisher Port (Outbound Port)
 * Responsibility: Publish domain events to infrastructure
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: Spring ApplicationEventPublisher, Kafka, RabbitMQ
 * 
 * Shared across User and Auth subdomains
 */
public interface DomainEventPublisher {
    
    /**
     * Publish a single domain event
     * 
     * @param event The domain event to publish
     */
    void publish(DomainEvent event);
}
