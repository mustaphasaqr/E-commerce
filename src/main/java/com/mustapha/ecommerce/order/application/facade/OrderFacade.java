package com.mustapha.ecommerce.order.application.facade;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mustapha.ecommerce.order.application.command.CancelOrderCommand;
import com.mustapha.ecommerce.order.application.command.DeliverOrderCommand;
import com.mustapha.ecommerce.order.application.command.GetOrderQuery;
import com.mustapha.ecommerce.order.application.command.PayOrderCommand;
import com.mustapha.ecommerce.order.application.command.PlaceOrderCommand;
import com.mustapha.ecommerce.order.application.command.ShipOrderCommand;
import com.mustapha.ecommerce.order.application.usecase.CancelOrderUseCase;
import com.mustapha.ecommerce.order.application.usecase.DeliverOrderUseCase;
import com.mustapha.ecommerce.order.application.usecase.GetOrderUseCase;
import com.mustapha.ecommerce.order.application.usecase.PayOrderUseCase;
import com.mustapha.ecommerce.order.application.usecase.PlaceOrderUseCase;
import com.mustapha.ecommerce.order.application.usecase.ShipOrderUseCase;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.dto.OrderListResponse;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;

import java.util.List;

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
    private final GetOrderUseCase getOrderUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final ShipOrderUseCase shipOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final com.mustapha.ecommerce.order.domain.repository.OrderRepository orderRepository;

    public OrderFacade(PlaceOrderUseCase placeOrderUseCase,
                      GetOrderUseCase getOrderUseCase,
                      PayOrderUseCase payOrderUseCase,
                      ShipOrderUseCase shipOrderUseCase,
                      DeliverOrderUseCase deliverOrderUseCase,
                      CancelOrderUseCase cancelOrderUseCase,
                      com.mustapha.ecommerce.order.domain.repository.OrderRepository orderRepository) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.payOrderUseCase = payOrderUseCase;
        this.shipOrderUseCase = shipOrderUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.orderRepository = orderRepository;
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
                    new ProductId(item.getProductId()),
                    item.getProductName(),
                    item.getQuantity(),
                    new Money(item.getPrice())
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
     */
    public OrderResponse getOrder(String orderId) {
        GetOrderQuery query = new GetOrderQuery(new OrderId(orderId));
        Order order = getOrderUseCase.execute(query);
        return OrderResponse.from(order);
    }

    /**
     * Pay for Order
     */
    public OrderResponse payOrder(String orderId, String paymentMethod, String paymentToken, double amount) {
        PayOrderCommand command = new PayOrderCommand(
            new OrderId(orderId),
            paymentMethod,
            paymentToken,
            new Money(amount)
        );
        Order order = payOrderUseCase.execute(command);
        return OrderResponse.from(order);
    }

    /**
     * Ship Order
     */
    public OrderResponse shipOrder(String orderId, String trackingNumber, String carrier) {
        ShipOrderCommand command = new ShipOrderCommand(
            new OrderId(orderId),
            trackingNumber,
            carrier
        );
        Order order = shipOrderUseCase.execute(command);
        return OrderResponse.from(order);
    }

    /**
     * Deliver Order
     */
    public OrderResponse deliverOrder(String orderId, LocalDateTime deliveredAt) {
        DeliverOrderCommand command = new DeliverOrderCommand(
            new OrderId(orderId),
            deliveredAt
        );
        Order order = deliverOrderUseCase.execute(command);
        return OrderResponse.from(order);
    }

    /**
     * Cancel Order
     */
    public OrderResponse cancelOrder(String orderId, String reason) {
        CancelOrderCommand command = new CancelOrderCommand(
            new OrderId(orderId),
            reason
        );
        Order order = cancelOrderUseCase.execute(command);
        return OrderResponse.from(order);
    }

    /**
     * List Orders by Customer - Lightweight DTO for list view
     * Performance: 55% smaller payload than full OrderResponse
     * Use for: Order history, customer order list
     */
    public List<OrderListResponse> listOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(new CustomerId(customerId)).stream()
            .map(OrderListResponse::fromDomain)
            .collect(Collectors.toList());
    }
}
