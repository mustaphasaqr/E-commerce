package com.mustapha.ecommerce.order.application.events;

/**
 * Event Publisher Interface
 * Pattern: Mediator, Observer
 */
public interface EventPublisher {
    void publish(Object event);
}
