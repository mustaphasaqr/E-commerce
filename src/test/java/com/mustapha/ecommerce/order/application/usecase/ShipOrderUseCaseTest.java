package com.mustapha.ecommerce.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.application.command.ShipOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

@DisplayName("ShipOrderUseCase")
class ShipOrderUseCaseTest {

    private OrderRepository orderRepository;
    private DomainEventPublisher eventPublisher;
    private NotificationPort notificationPort;
    private ShipOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        notificationPort = mock(NotificationPort.class);
        useCase = new ShipOrderUseCase(orderRepository, eventPublisher, notificationPort);
    }

    private Order createProcessingOrder() {
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(
                new ProductId("product-1"),
                "Test Product",
                2,
                new Money(50.0)
            ))
            .build();
        order.confirm();
        order.markAsPaid();
        order.startProcessing();
        return order;
    }

    @Test
    @DisplayName("Should ship order and store tracking information")
    void shouldShipOrderSuccessfully() {
        // Given
        Order order = createProcessingOrder();
        ShipOrderCommand command = new ShipOrderCommand(
            order.getId(),
            "TRACK123456",
            "FedEx"
        );

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = useCase.execute(command);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(result.getTrackingNumber()).isEqualTo("TRACK123456");
        assertThat(result.getCarrier()).isEqualTo("FedEx");
        verify(orderRepository).save(order);
        verify(notificationPort).sendOrderShipped(
            order.getCustomerId(),
            order.getId(),
            "TRACK123456",
            "FedEx"
        );
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        OrderId nonExistent = new OrderId("non-existent");
        ShipOrderCommand command = new ShipOrderCommand(nonExistent, "TRACK123", "UPS");

        when(orderRepository.findById(nonExistent)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should auto-transition from PAID to PROCESSING before shipping")
    void shouldAutoTransitionFromPaidToProcessing() {
        // Given - Order in PAID state (not yet PROCESSING)
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(new ProductId("p1"), "Product", 1, new Money(50.0)))
            .build();
        order.confirm();
        order.markAsPaid();
        // NOT calling startProcessing() - use case should do it automatically

        ShipOrderCommand command = new ShipOrderCommand(
            order.getId(),
            "TRACK-AUTO-123",
            "DHL"
        );

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = useCase.execute(command);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(result.getTrackingNumber()).isEqualTo("TRACK-AUTO-123");
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should throw exception when order not in PAID or PROCESSING state")
    void shouldThrowExceptionWhenNotPaidOrProcessing() {
        // Given - Order in CONFIRMED state (cannot ship from here)
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(new ProductId("p1"), "Product", 1, new Money(50.0)))
            .build();
        order.confirm();

        ShipOrderCommand command = new ShipOrderCommand(
            order.getId(),
            "TRACK123",
            "DHL"
        );

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
            .hasMessageContaining("Cannot transition to SHIPPED");
    }
}
