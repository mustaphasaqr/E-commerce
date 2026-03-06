package com.mustapha.ecommerce.cart.infrastructure.messaging;

import com.mustapha.ecommerce.cart.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.cart.domain.event.CartDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Cart Domain Event Publisher Adapter
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * Uses Spring ApplicationEventPublisher for in-memory event publishing.
 * Events are delivered synchronously within the same JVM.
 * 
 * For microservices, swap to Kafka/RabbitMQ implementation.
 * 
 * Published Events:
 * - CartCreatedEvent → Analytics (conversion funnel)
 * - CartItemAddedEvent → Product (popularity tracking), Analytics (funnel)
 * - CartItemRemovedEvent → Analytics (funnel)
 * - CartConvertedEvent → Analytics (conversion rate)
 * - CartAbandonedEvent → Marketing (recovery emails), Analytics
 */
@Primary
@Component
public class CartDomainEventPublisherAdapter implements DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(CartDomainEventPublisherAdapter.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public CartDomainEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(CartDomainEvent event) {
        // Publish in-memory using Spring Events
        applicationEventPublisher.publishEvent(event);
        
        logger.debug("📢 Published cart event: {} - Cart ID: {}", 
            event.getEventType(),
            event.getCartId().getValue());
    }
}
