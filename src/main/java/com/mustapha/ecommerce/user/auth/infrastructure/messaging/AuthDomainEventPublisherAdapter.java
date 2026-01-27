package com.mustapha.ecommerce.user.auth.infrastructure.messaging;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Auth Domain Event Publisher Adapter
 * Responsibility: Publish Auth domain events using Spring's ApplicationEventPublisher
 * Pattern: Adapter (Domain Port → Spring Infrastructure)
 * 
 * Scope: AUTH bounded context
 * Used by: Auth use cases (LoginUseCase, RefreshTokenUseCase, etc.)
 * 
 * Note: Auth reuses User's DomainEventPublisher port interface
 * Why: Auth events (RefreshTokenCreatedEvent, SessionCreatedEvent) extend DomainEvent from User domain
 * Alternative: Create auth.application.port.AuthDomainEventPublisher if needed
 * 
 * Implementation: Uses Spring's ApplicationEventPublisher for in-process events
 * Future: Can be replaced with Kafka/RabbitMQ for async/distributed events
 */
@Component
public class AuthDomainEventPublisherAdapter implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public AuthDomainEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
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
