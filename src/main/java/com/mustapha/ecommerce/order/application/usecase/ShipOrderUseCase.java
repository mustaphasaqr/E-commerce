package com.mustapha.ecommerce.order.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.ShipOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Ship Order Use Case
 * Responsibility: Orchestrate order shipment
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Retrieve order from repository
 * 2. Call order.ship() with tracking info
 * 3. Save & publish events
 * 4. Send shipping notification
 */
@Component
public class ShipOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;
    private final NotificationPort notificationPort;

    public ShipOrderUseCase(OrderRepository orderRepository,
                           DomainEventPublisher eventPublisher,
                           NotificationPort notificationPort) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public Order execute(ShipOrderCommand command) {
        // Step 1: Retrieve order
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.getOrderId().getValue()));
        
        // Step 2a: Auto-transition PAID → PROCESSING if needed (convenience for users)
        if (order.getStatus() == OrderStatus.PAID) {
            order.startProcessing();
        }
        
        // Step 2b: Call domain method (domain validates state transition and stores shipping info)
        order.ship(command.getTrackingNumber(), command.getCarrier());
        
        // Step 3: Save & publish events
        Order savedOrder = orderRepository.save(order);
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
        
        // Step 4: Send notification
        notificationPort.sendOrderShipped(
            order.getCustomerId(),
            order.getId(),
            command.getTrackingNumber(),
            command.getCarrier()
        );
        
        return savedOrder;
    }
}
