package com.mustapha.ecommerce.order.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.application.strategy.PaymentStrategy;
import com.mustapha.ecommerce.order.application.strategy.PaymentStrategyFactory;
import com.mustapha.ecommerce.order.application.validation.OrderValidator;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.order.domain.service.PricingService;
import com.mustapha.ecommerce.order.dto.OrderRequest;

/**
 * Create Order Use Case
 * Responsibility: Coordinate order creation business flow
 * Pattern: Unit of Work (@Transactional)
 */
@Component
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final PricingService pricingService;
    private final InventoryPort inventoryPort;
    private final OrderValidator orderValidator;
    private final PaymentStrategyFactory paymentStrategyFactory;

    public CreateOrderUseCase(OrderRepository orderRepository,
                             PricingService pricingService,
                             InventoryPort inventoryPort,
                             OrderValidator orderValidator,
                             PaymentStrategyFactory paymentStrategyFactory) {
        this.orderRepository = orderRepository;
        this.pricingService = pricingService;
        this.inventoryPort = inventoryPort;
        this.orderValidator = orderValidator;
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    @Transactional
    public Order execute(OrderRequest request) {
        // Validate order
        orderValidator.validate(request);
        
        // Reserve inventory
        inventoryPort.reserve(request.getItems());
        
        // Build order with pricing
        Order order = new OrderBuilder()
                .withCustomerId(request.getCustomerId())
                .withItems(request.getItems())
                .withPricingService(pricingService)
                .build();
        
        // Process payment
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(request.getPaymentMethod());
        paymentStrategy.processPayment(order.getTotalAmount(), request.getPaymentDetails());
        
        // Save order
        return orderRepository.save(order);
    }
}
