package com.mustapha.ecommerce.order.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.domain.DomainEvent;

/**
 * Domain Event Publisher Adapter
 * Implements DomainEventPublisher port for infrastructure layer
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * Uses Spring ApplicationEventPublisher for in-memory event publishing.
 * Events are published synchronously within the same JVM.
 * 
 * Events Published:
 * - OrderPlacedEvent → Analytics, Email notifications
 * - OrderPaidEvent → Analytics, Fulfillment, Revenue tracking
 * - OrderCancelledEvent → Inventory, Refund processing
 * - OrderShippedEvent → Email notifications, Tracking updates
 * - OrderDeliveredEvent → Analytics, Customer satisfaction
 * 
 * Future Enhancements:
 * - Swap to Kafka for distributed microservices
 * - Swap to RabbitMQ for reliable message delivery
 * - Add event persistence for audit trail
 */
@Primary
@Component
public class OrderDomainEventPublisherAdapter implements DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDomainEventPublisherAdapter.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public OrderDomainEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(DomainEvent event) {
        // Publish to Spring's ApplicationEventPublisher (in-memory, synchronous)
        applicationEventPublisher.publishEvent(event);
        
        logger.debug("Published domain event: {} - ID: {} - Occurred at: {}", 
            event.getClass().getSimpleName(),
            event.getEventId(),
            event.getOccurredAt());
    }
}
