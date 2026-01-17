package com.mustapha.ecommerce.order.application.facade;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mustapha.ecommerce.order.application.command.PlaceOrderCommand;
import com.mustapha.ecommerce.order.application.usecase.PlaceOrderUseCase;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;

/**
 * Order Facade - Translation Layer between API and Application
 * 
 * Responsibilities:
 * 1. Accept API DTOs (OrderRequest with primitives)
 * 2. Convert primitives → value objects → Commands
 * 3. Delegate to Use Cases (no business logic here)
 * 4. Convert Domain → API DTOs (OrderResponse)
 * 
 * What this is NOT:
 * - NOT a business logic layer (that's in Domain)
 * - NOT a transaction manager (that's in Use Cases)
 * - NOT an event publisher (that's in Use Cases)
 * - NOT a data access layer (that's in Repository)
 * 
 * Pattern: Facade, Anti-Corruption Layer
 * Think: "Translator + Router"
 */
@Service
public class OrderFacade {

    private final PlaceOrderUseCase placeOrderUseCase;
    // TODO: Add GetOrderUseCase when implemented
    // TODO: Add CancelOrderUseCase when implemented
    // TODO: Add ConfirmOrderUseCase when implemented
    // TODO: Add PayOrderUseCase when implemented

    public OrderFacade(PlaceOrderUseCase placeOrderUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
    }

    /**
     * Create Order - Entry point from REST API
     * 
     * Flow:
     * 1. Accept OrderRequest (API DTO with String/int/double primitives)
     * 2. Convert to PlaceOrderCommand (with CustomerId, ProductId, Money value objects)
     * 3. Call PlaceOrderUseCase (handles business orchestration + @Transactional)
     * 4. Return OrderResponse (API DTO)
     */
    public OrderResponse createOrder(OrderRequest request) {
        // Step 1: Convert API DTO → Application Command
        PlaceOrderCommand command = new PlaceOrderCommand(
            new CustomerId(request.getCustomerId()),
            request.getItems().stream()
                .map(item -> new PlaceOrderCommand.OrderItemData(
                    new ProductId(item.get("productId").toString()),
                    item.get("productName").toString(),
                    (Integer) item.get("quantity"),
                    new Money((Double) item.get("price"))
                ))
                .collect(Collectors.toList())
        );
        
        // Step 2: Delegate to Use Case (handles @Transactional, events, business rules)
        Order order = placeOrderUseCase.execute(command);
        
        // Step 3: Convert Domain → API DTO
        return OrderResponse.from(order);
    }

    /**
     * Get Order by ID
     * TODO: Implement GetOrderUseCase first
     */
    public OrderResponse getOrder(String orderId) {
        throw new UnsupportedOperationException("GetOrderUseCase not implemented yet");
    }

    /**
     * Cancel Order
     * TODO: Implement CancelOrderUseCase first
     */
    public void cancelOrder(String orderId) {
        throw new UnsupportedOperationException("CancelOrderUseCase not implemented yet");
    }
}
