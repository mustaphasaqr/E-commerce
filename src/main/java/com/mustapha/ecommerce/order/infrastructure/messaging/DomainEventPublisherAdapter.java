package com.mustapha.ecommerce.order.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.domain.DomainEvent;

/**
 * Domain Event Publisher Adapter
 * Implements DomainEventPublisher port for infrastructure layer
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * Currently logs events - can be enhanced to:
 * - Publish to Kafka
 * - Publish to RabbitMQ
 * - Use Spring ApplicationEventPublisher
 */
@Primary
@Component
public class DomainEventPublisherAdapter implements DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(DomainEventPublisherAdapter.class);
    
    // TODO: Inject Kafka/RabbitMQ/Spring ApplicationEventPublisher when ready
    
    @Override
    public void publish(DomainEvent event) {
        // For now, just log the event
        // In production, this would publish to Kafka/RabbitMQ/etc.
        logger.info("Publishing domain event: {} - ID: {} - Occurred at: {}", 
            event.getClass().getSimpleName(),
            event.getEventId(),
            event.getOccurredAt());
        
        // TODO: Implement actual event publishing
        // Example: kafkaTemplate.send("order-events", event);
        // Example: applicationEventPublisher.publishEvent(event);
    }
}
