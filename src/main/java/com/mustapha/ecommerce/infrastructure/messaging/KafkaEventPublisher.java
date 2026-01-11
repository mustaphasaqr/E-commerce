package com.mustapha.ecommerce.infrastructure.messaging;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.events.EventPublisher;

/**
 * Kafka Event Publisher
 * Responsibility: Publish events to Kafka
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    @Override
    public void publish(Object event) {
        // Publish to Kafka
        System.out.println("Publishing event to Kafka: " + event.getClass().getSimpleName());
    }
}
