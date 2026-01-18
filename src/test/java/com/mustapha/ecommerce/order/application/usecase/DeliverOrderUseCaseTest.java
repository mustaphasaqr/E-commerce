package com.mustapha.ecommerce.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.application.command.DeliverOrderCommand;
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

@DisplayName("DeliverOrderUseCase")
class DeliverOrderUseCaseTest {

    private OrderRepository orderRepository;
    private DomainEventPublisher eventPublisher;
    private NotificationPort notificationPort;
    private DeliverOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        notificationPort = mock(NotificationPort.class);
        useCase = new DeliverOrderUseCase(orderRepository, eventPublisher, notificationPort);
    }

    private Order createShippedOrder() {
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(
                new ProductId("product-1"),
                "Test Product",
                1,
                new Money(50.0)
            ))
            .build();
        order.confirm();
        order.markAsPaid();
        order.startProcessing();
        order.ship("TRACK123", "FedEx");
        return order;
    }

    @Test
    @DisplayName("Should deliver order and store delivered timestamp")
    void shouldDeliverOrderSuccessfully() {
        // Given
        Order order = createShippedOrder();
        LocalDateTime deliveredTime = LocalDateTime.now();
        DeliverOrderCommand command = new DeliverOrderCommand(
            order.getId(),
            deliveredTime
        );

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = useCase.execute(command);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(result.getDeliveredAt()).isEqualTo(deliveredTime);
        verify(orderRepository).save(order);
        verify(notificationPort).sendOrderDelivered(
            order.getCustomerId(),
            order.getId()
        );
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        OrderId nonExistent = new OrderId("non-existent");
        DeliverOrderCommand command = new DeliverOrderCommand(nonExistent, LocalDateTime.now());

        when(orderRepository.findById(nonExistent)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should throw exception when order not shipped")
    void shouldThrowExceptionWhenNotShipped() {
        // Given - Order in PAID state
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(new ProductId("p1"), "Product", 1, new Money(50.0)))
            .build();
        order.confirm();
        order.markAsPaid();

        DeliverOrderCommand command = new DeliverOrderCommand(
            order.getId(),
            LocalDateTime.now()
        );

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
            .hasMessageContaining("Cannot transition to DELIVERED");
    }
}
