package com.mustapha.ecommerce.order.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.GetOrderQuery;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.order.infrastructure.exception.OrderNotFoundException;

/**
 * Get Order Use Case
 * Responsibility: Retrieve order by ID
 * Pattern: Query (CQRS - read-only, no state changes)
 * 
 * Flow:
 * 1. Retrieve order from repository
 * 2. Return order (no modifications)
 * 
 * Note: Read-only, no domain events, no notifications
 */
@Component
public class GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Order execute(GetOrderQuery query) {
        return orderRepository.findById(query.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(query.getOrderId()));
    }
}
