package com.mustapha.ecommerce.product.application.port;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;

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
    void publish(ProductDomainEvent event);
}
