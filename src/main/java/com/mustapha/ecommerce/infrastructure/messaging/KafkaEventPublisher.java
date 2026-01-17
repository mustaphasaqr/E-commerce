package com.mustapha.ecommerce.infrastructure.messaging;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.domain.DomainEvent;

/**
 * Kafka Event Publisher (Infrastructure Implementation)
 * Implements: DomainEventPublisher port
 * Responsibility: Publish domain events to Kafka message broker
 * Pattern: Adapter (Hexagonal Architecture)
 */
@Component
public class KafkaEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(DomainEvent event) {
        // TODO: Implement actual Kafka publishing logic
        // For now, just log to console
        System.out.println("Publishing domain event to Kafka: " + event.getClass().getSimpleName() + 
                          " [ID: " + event.getEventId() + ", Timestamp: " + event.getOccurredAt() + "]");
    }
}
