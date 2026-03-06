package com.mustapha.ecommerce.cart.application.port;

import com.mustapha.ecommerce.cart.domain.event.CartDomainEvent;

/**
 * Domain Event Publisher Port (Outbound Port)
 * Responsibility: Publish cart domain events to infrastructure
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: Spring ApplicationEventPublisher, Kafka, RabbitMQ
 */
public interface DomainEventPublisher {
    
    /**
     * Publish a single cart domain event
     * 
     * @param event The cart domain event to publish
     */
    void publish(CartDomainEvent event);
}
