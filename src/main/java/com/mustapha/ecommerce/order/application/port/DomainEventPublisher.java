package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.domain.DomainEvent;

/**
 * Domain Event Publisher Port (Outbound Port)
 * Responsibility: Publish domain events to infrastructure
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: Kafka publisher, RabbitMQ publisher, Spring ApplicationEventPublisher
 */
public interface DomainEventPublisher {
    
    /**
     * Publish a single domain event
     * 
     * @param event The domain event to publish
     */
    void publish(DomainEvent event);
}
