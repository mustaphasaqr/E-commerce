package com.mustapha.ecommerce.order.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.PayOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Pay Order Use Case
 * Responsibility: Orchestrate payment processing
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Retrieve order from repository
 * 2. Process payment via PaymentPort
 * 3. Call order.pay() if payment succeeds
 * 4. Save & publish events
 * 5. Send payment confirmation notification
 */
@Component
public class PayOrderUseCase {

    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;
    private final DomainEventPublisher eventPublisher;
    private final NotificationPort notificationPort;

    public PayOrderUseCase(OrderRepository orderRepository, 
                          PaymentPort paymentPort,
                          DomainEventPublisher eventPublisher,
                          NotificationPort notificationPort) {
        this.orderRepository = orderRepository;
        this.paymentPort = paymentPort;
        this.eventPublisher = eventPublisher;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public Order execute(PayOrderCommand command) {
        // Step 1: Retrieve order
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.getOrderId().getValue()));
        
        // Step 2: Create payment checkout
        // TODO: This should return redirect URL and complete payment after customer pays
        // For now, we create checkout and assume immediate payment for backward compatibility
        PaymentPort.CheckoutResult checkoutResult = paymentPort.createCheckout(
            command.getOrderId(),
            command.getAmount(),
            command.getPaymentMethod(),
            order.getCustomerId().getValue() + "@example.com" // TODO: Get actual email
        );
        
        if (!checkoutResult.success()) {
            throw new IllegalStateException("Payment checkout failed: " + checkoutResult.message());
        }
        
        // Step 3: Call domain method (domain validates state transition)
        order.markAsPaid();
        
        // Step 4: Save & publish events
        Order savedOrder = orderRepository.save(order);
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
        
        // Step 5: Send notification
        notificationPort.sendPaymentReceived(
            order.getCustomerId(),
            order.getId()
        );
        
        return savedOrder;
    }
}
