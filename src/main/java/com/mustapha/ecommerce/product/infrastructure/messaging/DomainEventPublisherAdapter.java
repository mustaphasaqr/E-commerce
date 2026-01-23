package com.mustapha.ecommerce.product.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.ProductDomainEvent;

/**
 * Domain Event Publisher Adapter (Monolith Implementation)
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * Uses Spring ApplicationEventPublisher for in-memory event publishing.
 * Events are delivered synchronously within the same JVM.
 * 
 * For microservices, swap to Kafka/RabbitMQ implementation.
 */
@Primary
@Component
public class DomainEventPublisherAdapter implements DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisherAdapter.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public DomainEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(ProductDomainEvent event) {
        // Publish in-memory using Spring Events
        applicationEventPublisher.publishEvent(event);
        
        logger.debug("Published domain event: {} - ID: {}", 
            event.getClass().getSimpleName(),
            event.getEventId());
    }
}
