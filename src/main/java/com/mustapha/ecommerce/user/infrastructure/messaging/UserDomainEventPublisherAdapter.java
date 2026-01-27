package com.mustapha.ecommerce.user.infrastructure.messaging;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * User Domain Event Publisher Adapter
 * Responsibility: Publish User domain events using Spring's ApplicationEventPublisher
 * Pattern: Adapter (Domain Port → Spring Infrastructure)
 * 
 * Scope: USER bounded context only
 * Used by: User use cases (RegisterUserUseCase, ActivateUserUseCase, etc.)
 * 
 * Implementation: Uses Spring's ApplicationEventPublisher for in-process events
 * Future: Can be replaced with Kafka/RabbitMQ for async/distributed events
 */
@Component
public class UserDomainEventPublisherAdapter implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public UserDomainEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        // Publish to Spring's event system (synchronous by default)
        applicationEventPublisher.publishEvent(event);
        
        // TODO (Week 4+): Add async publishing to message broker
        // - Kafka for distributed systems
        // - RabbitMQ for guaranteed delivery
        // - Redis Pub/Sub for simple use cases
    }
}
