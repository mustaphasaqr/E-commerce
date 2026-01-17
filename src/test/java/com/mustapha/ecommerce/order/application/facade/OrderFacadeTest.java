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

import com.mustapha.ecommerce.order.application.command.PlaceOrderCommand;
import com.mustapha.ecommerce.order.application.usecase.PlaceOrderUseCase;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;

@DisplayName("OrderFacade Tests")
class OrderFacadeTest {

    private PlaceOrderUseCase placeOrderUseCase;
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        placeOrderUseCase = mock(PlaceOrderUseCase.class);
        orderFacade = new OrderFacade(placeOrderUseCase);
    }

    @Nested
    @DisplayName("Create Order - Translation Tests")
    class CreateOrderTranslationTests {

        @Test
        @DisplayName("Should translate OrderRequest to PlaceOrderCommand correctly")
        void shouldTranslateOrderRequestToCommand() {
            // Arrange
            OrderRequest request = createValidOrderRequest("CUST-001", 
                createItemMap("PROD-001", "Laptop", 2, 50.0)
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
                createItemMap("PROD-001", "Laptop", 2, 50.0),
                createItemMap("PROD-002", "Mouse", 1, 30.0),
                createItemMap("PROD-003", "Keyboard", 3, 20.0)
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
                createItemMap("PROD-001", "Product", 1, 100.0)
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
                createItemMap("PROD-123", "TestProduct", 5, 19.99)
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
                createItemMap("PROD-001", "Product", 1, 50.0)
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
                createItemMap("PROD-001", "Product", 1, 50.0)
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
                createItemMap("PROD-001", "Laptop", 2, 50.0)
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
                createItemMap("PROD-001", "Product", 1, 100.0)
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
                createItemMap("PROD-001", "Monitor", 3, 150.0),
                createItemMap("PROD-002", "Cable", 2, 25.0)
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
    @DisplayName("Unimplemented Methods")
    class UnimplementedMethodsTests {

        @Test
        @DisplayName("getOrder should throw UnsupportedOperationException")
        void getOrderShouldThrowException() {
            assertThatThrownBy(() -> orderFacade.getOrder("ORDER-001"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("GetOrderUseCase not implemented yet");
        }

        @Test
        @DisplayName("cancelOrder should throw UnsupportedOperationException")
        void cancelOrderShouldThrowException() {
            assertThatThrownBy(() -> orderFacade.cancelOrder("ORDER-001"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("CancelOrderUseCase not implemented yet");
        }
    }

    // ========== Helper Methods ==========

    @SafeVarargs
    private final OrderRequest createValidOrderRequest(String customerId, Map<String, Object>... items) {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(customerId);
        
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (Map<String, Object> item : items) {
            itemList.add(item);
        }
        request.setItems(itemList);
        
        return request;
    }

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
