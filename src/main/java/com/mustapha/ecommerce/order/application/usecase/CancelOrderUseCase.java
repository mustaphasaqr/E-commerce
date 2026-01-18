package com.mustapha.ecommerce.order.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.CancelOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Cancel Order Use Case
 * Responsibility: Orchestrate order cancellation
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Retrieve order from repository
 * 2. If order was paid, process refund via PaymentPort
 * 3. Call order.cancel()
 * 4. Save & publish events
 * 5. Send cancellation notification
 */
@Component
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;
    private final DomainEventPublisher eventPublisher;
    private final NotificationPort notificationPort;

    public CancelOrderUseCase(OrderRepository orderRepository,
                             PaymentPort paymentPort,
                             DomainEventPublisher eventPublisher,
                             NotificationPort notificationPort) {
        this.orderRepository = orderRepository;
        this.paymentPort = paymentPort;
        this.eventPublisher = eventPublisher;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public Order execute(CancelOrderCommand command) {
        // Step 1: Retrieve order
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.getOrderId().getValue()));
        
        // Step 2: Process refund if order was paid (covers PAID, PROCESSING, SHIPPED, DELIVERED)
        if (order.isPaid()) {
            PaymentPort.PaymentResult refundResult = paymentPort.refundPayment(
                order.getId(),
                order.getTotalAmount()
            );
            
            if (!refundResult.isSuccess()) {
                throw new IllegalStateException("Refund failed: " + refundResult.errorMessage());
            }
        }
        
        // Step 3: Call domain method (domain validates state transition and stores reason)
        order.cancel(command.getReason());
        
        // Step 4: Save & publish events
        Order savedOrder = orderRepository.save(order);
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
        
        // Step 5: Send notification
        notificationPort.sendOrderCancelled(
            order.getCustomerId(),
            order.getId(),
            command.getReason()
        );
        
        return savedOrder;
    }
}
