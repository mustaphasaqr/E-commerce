package com.mustapha.ecommerce.order.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.PlaceOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Place Order Use Case
 * Responsibility: Orchestrate order placement (NOT business rules)
 * Pattern: Use Case (Application Service)
 * 
 * Clean 4-Step Pattern:
 * 1. Take input (PlaceOrderCommand)
 * 2. Create aggregate (Order.place factory)
 * 3. Call domain methods (addItem, confirm)
 * 4. Save & publish events
 */
@Component
public class PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    public PlaceOrderUseCase(OrderRepository orderRepository, 
                            DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order execute(PlaceOrderCommand command) {
        // Step 1: Input already received as PlaceOrderCommand
        
        // Step 2: Build aggregate using OrderBuilder
        OrderBuilder builder = new OrderBuilder()
                .withCustomerId(command.getCustomerId());
        
        // Add items incrementally (already converted to value objects by Facade)
        for (PlaceOrderCommand.OrderItemData itemData : command.getItems()) {
            OrderItem item = new OrderItem(
                itemData.getProductId(),    // Already ProductId
                itemData.getProductName(),
                itemData.getQuantity(),
                itemData.getUnitPrice()     // Already Money
            );
            builder.addItem(item);
        }
        
        Order order = builder.build();
        
        // Step 3: Call domain methods (domain handles all business logic)
        // Confirm order (raises OrderPlacedEvent internally)
        order.confirm();
        
        // Step 4: Save & publish events
        Order savedOrder = orderRepository.save(order);
        
        // Publish domain events
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
        
        return savedOrder;
    }
}
