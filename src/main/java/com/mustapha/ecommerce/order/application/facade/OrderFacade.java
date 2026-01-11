package com.mustapha.ecommerce.order.application.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.events.EventPublisher;
import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.usecase.CreateOrderUseCase;
import com.mustapha.ecommerce.order.domain.event.OrderPlacedEvent;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;
import com.mustapha.ecommerce.order.exception.OrderNotFoundException;

/**
 * Application Facade - Orchestration Layer
 * Responsibility: Business flow coordination, Transaction boundaries
 * Pattern: Facade, Mediator, Unit of Work
 * SOLID: SRP, OCP, DIP
 */
@Service
public class OrderFacade {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;
    private final InventoryPort inventoryPort;
    private final EventPublisher eventPublisher;

    public OrderFacade(CreateOrderUseCase createOrderUseCase,
                      OrderRepository orderRepository,
                      PaymentPort paymentPort,
                      InventoryPort inventoryPort,
                      EventPublisher eventPublisher) {
        this.createOrderUseCase = createOrderUseCase;
        this.orderRepository = orderRepository;
        this.paymentPort = paymentPort;
        this.inventoryPort = inventoryPort;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Orchestrate the order creation flow
        Order order = createOrderUseCase.execute(request);
        
        // Publish domain event
        eventPublisher.publish(new OrderPlacedEvent(order.getId().getValue(), order.getTotalAmount()));
        
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return OrderResponse.from(order);
    }

    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        order.cancel();
        orderRepository.save(order);
    }
}
