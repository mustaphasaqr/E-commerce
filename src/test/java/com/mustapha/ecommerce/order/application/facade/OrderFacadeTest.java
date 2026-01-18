package com.mustapha.ecommerce.order.application.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;
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
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

@DisplayName("OrderFacade Tests")
class OrderFacadeTest {

    private PlaceOrderUseCase placeOrderUseCase;
    private GetOrderUseCase getOrderUseCase;
    private PayOrderUseCase payOrderUseCase;
    private ShipOrderUseCase shipOrderUseCase;
    private DeliverOrderUseCase deliverOrderUseCase;
    private CancelOrderUseCase cancelOrderUseCase;
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        placeOrderUseCase = mock(PlaceOrderUseCase.class);
        getOrderUseCase = mock(GetOrderUseCase.class);
        payOrderUseCase = mock(PayOrderUseCase.class);
        shipOrderUseCase = mock(ShipOrderUseCase.class);
        deliverOrderUseCase = mock(DeliverOrderUseCase.class);
        cancelOrderUseCase = mock(CancelOrderUseCase.class);
        orderFacade = new OrderFacade(
            placeOrderUseCase,
            getOrderUseCase,
            payOrderUseCase,
            shipOrderUseCase,
            deliverOrderUseCase,
            cancelOrderUseCase
        );
    }

    @Nested
    @DisplayName("Create Order - Translation Tests")
    class CreateOrderTranslationTests {

        @Test
        @DisplayName("Should translate OrderRequest to PlaceOrderCommand correctly")
        void shouldTranslateOrderRequestToCommand() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001", 
                createItemRequest("PROD-001", "Laptop", 2, 50.0)
            );

            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 2, 50.0);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<PlaceOrderCommand> commandCaptor = ArgumentCaptor.forClass(PlaceOrderCommand.class);

            // Act
            orderFacade.createOrder(request);

            // Assert
            verify(placeOrderUseCase).execute(commandCaptor.capture());
            PlaceOrderCommand capturedCommand = commandCaptor.getValue();
            
            assertThat(capturedCommand.getCustomerId()).isEqualTo(new CustomerId("CUST-001"));
            assertThat(capturedCommand.getItems()).hasSize(1);
            
            PlaceOrderCommand.OrderItemData itemData = capturedCommand.getItems().get(0);
            assertThat(itemData.getProductId()).isEqualTo(new ProductId("PROD-001"));
            assertThat(itemData.getProductName()).isEqualTo("Laptop");
            assertThat(itemData.getQuantity()).isEqualTo(2);
            assertThat(itemData.getUnitPrice()).isEqualTo(new Money(50.0));
        }

        @Test
        @DisplayName("Should translate multiple items correctly")
        void shouldTranslateMultipleItems() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001",
                createItemRequest("PROD-001", "Laptop", 2, 50.0),
                createItemRequest("PROD-002", "Mouse", 1, 30.0),
                createItemRequest("PROD-003", "Keyboard", 3, 20.0)
            );

            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 2, 50.0);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<PlaceOrderCommand> commandCaptor = ArgumentCaptor.forClass(PlaceOrderCommand.class);

            // Act
            orderFacade.createOrder(request);

            // Assert
            verify(placeOrderUseCase).execute(commandCaptor.capture());
            PlaceOrderCommand capturedCommand = commandCaptor.getValue();
            
            assertThat(capturedCommand.getItems()).hasSize(3);
            assertThat(capturedCommand.getItems().get(0).getProductId()).isEqualTo(new ProductId("PROD-001"));
            assertThat(capturedCommand.getItems().get(1).getProductId()).isEqualTo(new ProductId("PROD-002"));
            assertThat(capturedCommand.getItems().get(2).getProductId()).isEqualTo(new ProductId("PROD-003"));
        }

        @Test
        @DisplayName("Should convert CustomerId from String to value object")
        void shouldConvertCustomerId() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-999", 
                createItemRequest("PROD-001", "Product", 1, 100.0)
            );

            Order mockOrder = createMockOrder("CUST-999", "PROD-001", "Product", 1, 100.0);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<PlaceOrderCommand> commandCaptor = ArgumentCaptor.forClass(PlaceOrderCommand.class);

            // Act
            orderFacade.createOrder(request);

            // Assert
            verify(placeOrderUseCase).execute(commandCaptor.capture());
            PlaceOrderCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.getCustomerId()).isEqualTo(new CustomerId("CUST-999"));
        }

        @Test
        @DisplayName("Should convert primitives to value objects in items")
        void shouldConvertPrimitivesToValueObjects() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001",
                createItemRequest("PROD-123", "TestProduct", 5, 19.99)
            );

            Order mockOrder = createMockOrder("CUST-001", "PROD-123", "TestProduct", 5, 19.99);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<PlaceOrderCommand> commandCaptor = ArgumentCaptor.forClass(PlaceOrderCommand.class);

            // Act
            orderFacade.createOrder(request);

            // Assert
            verify(placeOrderUseCase).execute(commandCaptor.capture());
            PlaceOrderCommand.OrderItemData item = commandCaptor.getValue().getItems().get(0);
            
            assertThat(item.getProductId()).isInstanceOf(ProductId.class);
            assertThat(item.getUnitPrice()).isInstanceOf(Money.class);
            assertThat(item.getProductId()).isEqualTo(new ProductId("PROD-123"));
            assertThat(item.getUnitPrice()).isEqualTo(new Money(19.99));
        }
    }

    @Nested
    @DisplayName("Create Order - Use Case Delegation")
    class CreateOrderDelegationTests {

        @Test
        @DisplayName("Should delegate to PlaceOrderUseCase")
        void shouldDelegateToPlaceOrderUseCase() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001",
                createItemRequest("PROD-001", "Product", 1, 50.0)
            );

            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Product", 1, 50.0);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            // Act
            orderFacade.createOrder(request);

            // Assert
            verify(placeOrderUseCase, times(1)).execute(any(PlaceOrderCommand.class));
        }

        @Test
        @DisplayName("Should call use case exactly once per request")
        void shouldCallUseCaseOnce() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001",
                createItemRequest("PROD-001", "Product", 1, 50.0)
            );

            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Product", 1, 50.0);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            // Act
            orderFacade.createOrder(request);
            orderFacade.createOrder(request);

            // Assert
            verify(placeOrderUseCase, times(2)).execute(any(PlaceOrderCommand.class));
        }
    }

    @Nested
    @DisplayName("Create Order - Response Translation")
    class CreateOrderResponseTranslationTests {

        @Test
        @DisplayName("Should return OrderResponse from domain Order")
        void shouldReturnOrderResponse() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001",
                createItemRequest("PROD-001", "Laptop", 2, 50.0)
            );

            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 2, 50.0);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            // Act
            OrderResponse response = orderFacade.createOrder(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isNotNull();
            assertThat(response.getCustomerId()).isEqualTo("CUST-001");
            assertThat(response.getStatus()).isEqualTo("CONFIRMED");
            assertThat(response.getTotalAmount()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should translate Order status correctly")
        void shouldTranslateOrderStatus() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001",
                createItemRequest("PROD-001", "Product", 1, 100.0)
            );

            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Product", 1, 100.0);
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            // Act
            OrderResponse response = orderFacade.createOrder(request);

            // Assert
            assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        }

        @Test
        @DisplayName("Should include all order details in response")
        void shouldIncludeAllOrderDetails() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-789",
                createItemRequest("PROD-001", "Monitor", 3, 150.0),
                createItemRequest("PROD-002", "Cable", 2, 25.0)
            );

            CustomerId customerId = new CustomerId("CUST-789");
            OrderItem item1 = new OrderItem(new ProductId("PROD-001"), "Monitor", 3, new Money(150.0));
            OrderItem item2 = new OrderItem(new ProductId("PROD-002"), "Cable", 2, new Money(25.0));
            
            Order mockOrder = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item1)
                .addItem(item2)
                .build();
            mockOrder.confirm(); // Transition to CONFIRMED
            
            when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(mockOrder);

            // Act
            OrderResponse response = orderFacade.createOrder(request);

            // Assert
            assertThat(response.getCustomerId()).isEqualTo("CUST-789");
            assertThat(response.getTotalAmount()).isEqualTo(500.0); // (3 * 150) + (2 * 25) = 450 + 50 = 500
        }
    }

    @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should translate orderId string to GetOrderQuery")
        void shouldTranslateGetOrderParameters() {
            // Arrange
            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 1, 100.0);
            when(getOrderUseCase.execute(any(GetOrderQuery.class))).thenReturn(mockOrder);

            ArgumentCaptor<GetOrderQuery> queryCaptor = ArgumentCaptor.forClass(GetOrderQuery.class);

            // Act
            OrderResponse response = orderFacade.getOrder(mockOrder.getId().getValue());

            // Assert
            verify(getOrderUseCase).execute(queryCaptor.capture());
            assertThat(queryCaptor.getValue().getOrderId()).isEqualTo(mockOrder.getId());
            assertThat(response.getOrderId()).isEqualTo(mockOrder.getId().getValue());
        }
    }

    @Nested
    @DisplayName("Pay Order Tests")
    class PayOrderTests {

        @Test
        @DisplayName("Should translate pay parameters to PayOrderCommand")
        void shouldTranslatePayOrderParameters() {
            // Arrange
            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 1, 100.0);
            mockOrder.markAsPaid();
            when(payOrderUseCase.execute(any(PayOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<PayOrderCommand> commandCaptor = ArgumentCaptor.forClass(PayOrderCommand.class);

            // Act
            OrderResponse response = orderFacade.payOrder(
                mockOrder.getId().getValue(),
                "credit_card",
                "tok_visa",
                100.0
            );

            // Assert
            verify(payOrderUseCase).execute(commandCaptor.capture());
            PayOrderCommand cmd = commandCaptor.getValue();
            assertThat(cmd.getOrderId()).isEqualTo(mockOrder.getId());
            assertThat(cmd.getPaymentMethod()).isEqualTo("credit_card");
            assertThat(cmd.getPaymentToken()).isEqualTo("tok_visa");
            assertThat(cmd.getAmount()).isEqualTo(new Money(100.0));
            assertThat(response.getStatus()).isEqualTo("PAID");
        }
    }

    @Nested
    @DisplayName("Ship Order Tests")
    class ShipOrderTests {

        @Test
        @DisplayName("Should translate ship parameters to ShipOrderCommand")
        void shouldTranslateShipOrderParameters() {
            // Arrange
            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 1, 100.0);
            // mockOrder already confirmed by helper
            mockOrder.markAsPaid();
            mockOrder.startProcessing();
            mockOrder.ship("TRACK123", "FedEx");
            when(shipOrderUseCase.execute(any(ShipOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<ShipOrderCommand> commandCaptor = ArgumentCaptor.forClass(ShipOrderCommand.class);

            // Act
            OrderResponse response = orderFacade.shipOrder(
                mockOrder.getId().getValue(),
                "TRACK123",
                "FedEx"
            );

            // Assert
            verify(shipOrderUseCase).execute(commandCaptor.capture());
            ShipOrderCommand cmd = commandCaptor.getValue();
            assertThat(cmd.getOrderId()).isEqualTo(mockOrder.getId());
            assertThat(cmd.getTrackingNumber()).isEqualTo("TRACK123");
            assertThat(cmd.getCarrier()).isEqualTo("FedEx");
            assertThat(response.getStatus()).isEqualTo("SHIPPED");
        }
    }

    @Nested
    @DisplayName("Deliver Order Tests")
    class DeliverOrderTests {

        @Test
        @DisplayName("Should translate deliver parameters to DeliverOrderCommand")
        void shouldTranslateDeliverOrderParameters() {
            // Arrange
            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 1, 100.0);
            // mockOrder already confirmed by helper
            mockOrder.markAsPaid();
            mockOrder.startProcessing();
            mockOrder.ship("TRACK123", "FedEx");
            LocalDateTime deliveredAt = LocalDateTime.now();
            mockOrder.deliver(deliveredAt);
            when(deliverOrderUseCase.execute(any(DeliverOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<DeliverOrderCommand> commandCaptor = ArgumentCaptor.forClass(DeliverOrderCommand.class);

            // Act
            OrderResponse response = orderFacade.deliverOrder(
                mockOrder.getId().getValue(),
                deliveredAt
            );

            // Assert
            verify(deliverOrderUseCase).execute(commandCaptor.capture());
            DeliverOrderCommand cmd = commandCaptor.getValue();
            assertThat(cmd.getOrderId()).isEqualTo(mockOrder.getId());
            assertThat(cmd.getDeliveredAt()).isEqualTo(deliveredAt);
            assertThat(response.getStatus()).isEqualTo("DELIVERED");
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should translate cancel parameters to CancelOrderCommand")
        void shouldTranslateCancelOrderParameters() {
            // Arrange
            Order mockOrder = createMockOrder("CUST-001", "PROD-001", "Laptop", 1, 100.0);
            // mockOrder already confirmed by helper
            mockOrder.cancel("Customer request");
            when(cancelOrderUseCase.execute(any(CancelOrderCommand.class))).thenReturn(mockOrder);

            ArgumentCaptor<CancelOrderCommand> commandCaptor = ArgumentCaptor.forClass(CancelOrderCommand.class);

            // Act
            OrderResponse response = orderFacade.cancelOrder(
                mockOrder.getId().getValue(),
                "Customer request"
            );

            // Assert
            verify(cancelOrderUseCase).execute(commandCaptor.capture());
            CancelOrderCommand cmd = commandCaptor.getValue();
            assertThat(cmd.getOrderId()).isEqualTo(mockOrder.getId());
            assertThat(cmd.getReason()).isEqualTo("Customer request");
            assertThat(response.getStatus()).isEqualTo("CANCELLED");
        }
    }

    // ========== Helper Methods ==========

    @SafeVarargs
    private final OrderRequest createValidOrderRequest(String customerId, OrderItemRequest... items) {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(customerId);
        
        List<OrderItemRequest> itemList = new ArrayList<>();
        for (OrderItemRequest item : items) {
            itemList.add(item);
        }
        request.setItems(itemList);
        
        return request;
    }

    private OrderItemRequest createItemRequest(String productId, String productName, int quantity, double price) {
        return new OrderItemRequest(productId, productName, quantity, price);
    }

    // Deprecated - keeping for backward compatibility, redirects to new method
    private Map<String, Object> createItemMap(String productId, String productName, int quantity, double price) {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("productName", productName);
        item.put("quantity", quantity);
        item.put("price", price);
        return item;
    }

    private Order createMockOrder(String customerId, String productId, String productName, int quantity, double price) {
        CustomerId customerIdVO = new CustomerId(customerId);
        ProductId productIdVO = new ProductId(productId);
        Money priceVO = new Money(price);
        
        OrderItem item = new OrderItem(productIdVO, productName, quantity, priceVO);
        
        Order order = new OrderBuilder()
            .withCustomerId(customerIdVO)
            .addItem(item)
            .build();
        
        order.confirm(); // Transition to CONFIRMED status
        
        return order;
    }
}
