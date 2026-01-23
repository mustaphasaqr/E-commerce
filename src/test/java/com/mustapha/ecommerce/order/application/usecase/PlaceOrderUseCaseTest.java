package com.mustapha.ecommerce.order.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mustapha.ecommerce.order.application.command.PlaceOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.ProductPort;
import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.event.OrderPlacedEvent;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

@DisplayName("PlaceOrderUseCase Tests")
class PlaceOrderUseCaseTest {

    private OrderRepository orderRepository;
    private ProductPort productPort;
    private DomainEventPublisher eventPublisher;
    private PlaceOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productPort = mock(ProductPort.class);
        eventPublisher = mock(DomainEventPublisher.class);
        useCase = new PlaceOrderUseCase(orderRepository, productPort, eventPublisher);
        
        // Setup default ProductPort behavior - accept any price for tests
        when(productPort.productExists(any(ProductId.class))).thenReturn(true);
        when(productPort.getAvailableStock(any(ProductId.class))).thenReturn(1000); // Large stock by default
        when(productPort.isDiscontinued(any(ProductId.class))).thenReturn(false); // Not discontinued by default
        // Mock will be configured per test to return specific prices
    }
    
    private void setupProductPrices(Money... prices) {
        when(productPort.getProductPrice(any(ProductId.class)))
            .thenReturn(prices[0], prices.length > 1 ? java.util.Arrays.copyOfRange(prices, 1, prices.length) : new Money[0]);
    }

    @Nested
    @DisplayName("Successful Order Placement")
    class SuccessfulOrderPlacement {

        @Test
        @DisplayName("Should create order with single item")
        void shouldCreateOrderWithSingleItem() {
            // Arrange
            setupProductPrices(new Money(50.0));
            CustomerId customerId = new CustomerId("CUST-001");
            PlaceOrderCommand command = new PlaceOrderCommand(
                customerId,
                List.of(new PlaceOrderCommand.OrderItemData(
                    new ProductId("PROD-001"), 
                    "Laptop", 
                    2, 
                    new Money(50.0)
                ))
            );

            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(customerId);
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(result.getTotalAmount().getAmount()).isEqualTo(100.0); // 2 * 50
        }

        @Test
        @DisplayName("Should create order with multiple items")
        void shouldCreateOrderWithMultipleItems() {
            // Arrange
            setupProductPrices(new Money(50.0), new Money(30.0), new Money(20.0));
            CustomerId customerId = new CustomerId("CUST-001");
            PlaceOrderCommand command = new PlaceOrderCommand(
                customerId,
                Arrays.asList(
                    new PlaceOrderCommand.OrderItemData(new ProductId("PROD-001"), "Laptop", 2, new Money(50.0)),
                    new PlaceOrderCommand.OrderItemData(new ProductId("PROD-002"), "Mouse", 1, new Money(30.0)),
                    new PlaceOrderCommand.OrderItemData(new ProductId("PROD-003"), "Keyboard", 3, new Money(20.0))
                )
            );

            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result.getItems()).hasSize(3);
            assertThat(result.getTotalAmount().getAmount()).isEqualTo(190.0); // 100 + 30 + 60
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should transition order to CONFIRMED status")
        void shouldTransitionOrderToConfirmedStatus() {
            // Arrange
            PlaceOrderCommand command = createValidCommand();
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("Repository Interaction")
    class RepositoryInteraction {

        @Test
        @DisplayName("Should save order to repository")
        void shouldSaveOrderToRepository() {
            // Arrange
            PlaceOrderCommand command = createValidCommand();
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            useCase.execute(command);

            // Assert
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should save order with correct customer ID")
        void shouldSaveOrderWithCorrectCustomerId() {
            // Arrange
            setupProductPrices(new Money(100.0));
            CustomerId customerId = new CustomerId("CUST-123");
            PlaceOrderCommand command = new PlaceOrderCommand(
                customerId,
                List.of(new PlaceOrderCommand.OrderItemData(
                    new ProductId("PROD-001"), 
                    "Phone", 
                    1, 
                    new Money(100.0)
                ))
            );

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            useCase.execute(command);

            // Assert
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getCustomerId()).isEqualTo(customerId);
        }
    }

    @Nested
    @DisplayName("Domain Event Publishing")
    class DomainEventPublishing {

        @Test
        @DisplayName("Should publish OrderPlacedEvent when order is confirmed")
        void shouldPublishOrderPlacedEvent() {
            // Arrange
            PlaceOrderCommand command = createValidCommand();
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            useCase.execute(command);

            // Assert
            verify(eventPublisher, times(1)).publish(any(OrderPlacedEvent.class));
        }

        @Test
        @DisplayName("Should publish event with correct order details")
        void shouldPublishEventWithCorrectOrderDetails() {
            // Arrange
            setupProductPrices(new Money(75.0));
            CustomerId customerId = new CustomerId("CUST-999");
            PlaceOrderCommand command = new PlaceOrderCommand(
                customerId,
                List.of(new PlaceOrderCommand.OrderItemData(
                    new ProductId("PROD-001"), 
                    "Tablet", 
                    2, 
                    new Money(75.0)
                ))
            );

            ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            verify(eventPublisher).publish(eventCaptor.capture());
            DomainEvent publishedEvent = eventCaptor.getValue();
            
            assertThat(publishedEvent).isInstanceOf(OrderPlacedEvent.class);
            OrderPlacedEvent orderPlacedEvent = (OrderPlacedEvent) publishedEvent;
            assertThat(orderPlacedEvent.orderId()).isEqualTo(result.getId());
            assertThat(orderPlacedEvent.customerId()).isEqualTo(customerId);
            assertThat(orderPlacedEvent.totalAmount().getAmount()).isEqualTo(150.0);
        }

        @Test
        @DisplayName("Should clear domain events after publishing")
        void shouldClearDomainEventsAfterPublishing() {
            // Arrange
            PlaceOrderCommand command = createValidCommand();
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Order Total Calculation")
    class OrderTotalCalculation {

        @Test
        @DisplayName("Should calculate total for single item")
        void shouldCalculateTotalForSingleItem() {
            // Arrange
            setupProductPrices(new Money(20.0));
            PlaceOrderCommand command = new PlaceOrderCommand(
                new CustomerId("CUST-001"),
                List.of(new PlaceOrderCommand.OrderItemData(
                    new ProductId("PROD-001"), 
                    "Monitor", 
                    5, 
                    new Money(20.0)
                ))
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result.getTotalAmount().getAmount()).isEqualTo(100.0); // 5 * 20
        }

        @Test
        @DisplayName("Should calculate total for multiple items correctly")
        void shouldCalculateTotalForMultipleItems() {
            // Arrange
            setupProductPrices(new Money(25.0), new Money(15.5), new Money(100.0));
            PlaceOrderCommand command = new PlaceOrderCommand(
                new CustomerId("CUST-001"),
                Arrays.asList(
                    new PlaceOrderCommand.OrderItemData(new ProductId("PROD-001"), "Headphones", 3, new Money(25.0)),  // 75
                    new PlaceOrderCommand.OrderItemData(new ProductId("PROD-002"), "Cable", 2, new Money(15.5)),       // 31
                    new PlaceOrderCommand.OrderItemData(new ProductId("PROD-003"), "Webcam", 1, new Money(100.0))      // 100
                )
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result.getTotalAmount().getAmount()).isEqualTo(206.0);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Should handle order with large quantity")
        void shouldHandleOrderWithLargeQuantity() {
            // Arrange
            setupProductPrices(new Money(10.0));
            PlaceOrderCommand command = new PlaceOrderCommand(
                new CustomerId("CUST-001"),
                List.of(new PlaceOrderCommand.OrderItemData(
                    new ProductId("PROD-001"), 
                    "Book", 
                    50, 
                    new Money(10.0)
                ))
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result.getTotalAmount().getAmount()).isEqualTo(500.0);
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should handle order with decimal prices")
        void shouldHandleOrderWithDecimalPrices() {
            // Arrange
            setupProductPrices(new Money(19.99));
            PlaceOrderCommand command = new PlaceOrderCommand(
                new CustomerId("CUST-001"),
                List.of(new PlaceOrderCommand.OrderItemData(
                    new ProductId("PROD-001"), 
                    "Pen", 
                    3, 
                    new Money(19.99)
                ))
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Order result = useCase.execute(command);

            // Assert
            assertThat(result.getTotalAmount().getAmount()).isEqualTo(59.97);
        }
    }

    // Helper method to create valid command
    private PlaceOrderCommand createValidCommand() {
        setupProductPrices(new Money(50.0));
        return new PlaceOrderCommand(
            new CustomerId("CUST-001"),
            List.of(new PlaceOrderCommand.OrderItemData(
                new ProductId("PROD-001"), 
                "DefaultProduct", 
                2, 
                new Money(50.0)
            ))
        );
    }
}
