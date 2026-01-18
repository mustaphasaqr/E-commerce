package com.mustapha.ecommerce.order.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.DeliverOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Deliver Order Use Case
 * Responsibility: Orchestrate order delivery completion
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Retrieve order from repository
 * 2. Call order.deliver()
 * 3. Save & publish events
 * 4. Send delivery confirmation notification
 */
@Component
public class DeliverOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;
    private final NotificationPort notificationPort;

    public DeliverOrderUseCase(OrderRepository orderRepository,
                              DomainEventPublisher eventPublisher,
                              NotificationPort notificationPort) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public Order execute(DeliverOrderCommand command) {
        // Step 1: Retrieve order
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.getOrderId().getValue()));
        
        // Step 2: Call domain method (domain validates state transition and stores delivery time)
        order.deliver(command.getDeliveredAt());
        
        // Step 3: Save & publish events
        Order savedOrder = orderRepository.save(order);
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
        
        // Step 4: Send notification
        notificationPort.sendOrderDelivered(
            order.getCustomerId(),
            order.getId()
        );
        
        return savedOrder;
    }
}
