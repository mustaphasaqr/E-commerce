package com.mustapha.ecommerce.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.application.command.CancelOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

@DisplayName("CancelOrderUseCase")
class CancelOrderUseCaseTest {

    private OrderRepository orderRepository;
    private PaymentPort paymentPort;
    private DomainEventPublisher eventPublisher;
    private NotificationPort notificationPort;
    private CancelOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentPort = mock(PaymentPort.class);
        eventPublisher = mock(DomainEventPublisher.class);
        notificationPort = mock(NotificationPort.class);
        useCase = new CancelOrderUseCase(orderRepository, paymentPort, eventPublisher, notificationPort);
    }

    @Nested
    @DisplayName("Cancellation without refund")
    class CancellationWithoutRefund {

        @Test
        @DisplayName("Should cancel unpaid order without refund")
        void shouldCancelUnpaidOrder() {
            // Given - Order in CONFIRMED state (not paid)
            Order order = new OrderBuilder()
                .withCustomerId(new CustomerId("customer-123"))
                .addItem(new OrderItem(new ProductId("p1"), "Product", 1, new Money(50.0)))
                .build();
            order.confirm();

            CancelOrderCommand command = new CancelOrderCommand(
                order.getId(),
                "Customer changed mind"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Order result = useCase.execute(command);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(result.getCancellationReason()).isEqualTo("Customer changed mind");
            verify(paymentPort, never()).refundPayment(any(), any()); // No refund
            verify(notificationPort).sendOrderCancelled(
                order.getCustomerId(),
                order.getId(),
                "Customer changed mind"
            );
        }
    }

    @Nested
    @DisplayName("Cancellation with refund")
    class CancellationWithRefund {

        @Test
        @DisplayName("Should cancel paid order with refund")
        void shouldCancelPaidOrderWithRefund() {
            // Given
            Order order = new OrderBuilder()
                .withCustomerId(new CustomerId("customer-123"))
                .addItem(new OrderItem(new ProductId("p1"), "Product", 2, new Money(50.0)))
                .build();
            order.confirm();
            order.markAsPaid();

            CancelOrderCommand command = new CancelOrderCommand(
                order.getId(),
                "Out of stock"
            );

            PaymentPort.PaymentResult refundSuccess = new PaymentPort.PaymentResult(
                true,
                "refund_123",
                "Refund processed"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(paymentPort.refundPayment(order.getId(), order.getTotalAmount()))
                .thenReturn(refundSuccess);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Order result = useCase.execute(command);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(result.getCancellationReason()).isEqualTo("Out of stock");
            verify(paymentPort).refundPayment(order.getId(), new Money(100.0));
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Should throw exception when refund fails")
        void shouldThrowExceptionWhenRefundFails() {
            // Given
            Order order = new OrderBuilder()
                .withCustomerId(new CustomerId("customer-123"))
                .addItem(new OrderItem(new ProductId("p1"), "Product", 1, new Money(50.0)))
                .build();
            order.confirm();
            order.markAsPaid();

            CancelOrderCommand command = new CancelOrderCommand(order.getId(), "Reason");

            PaymentPort.PaymentResult refundFailure = new PaymentPort.PaymentResult(
                false,
                null,
                "Refund failed"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(paymentPort.refundPayment(any(), any())).thenReturn(refundFailure);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Refund failed");

            verify(orderRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        OrderId nonExistent = new OrderId("non-existent");
        CancelOrderCommand command = new CancelOrderCommand(nonExistent, "Reason");

        when(orderRepository.findById(nonExistent)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should throw exception when order already shipped")
    void shouldThrowExceptionWhenAlreadyShipped() {
        // Given - Shipped order cannot be cancelled
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(new ProductId("p1"), "Product", 1, new Money(50.0)))
            .build();
        order.confirm();
        order.markAsPaid();
        order.startProcessing();
        order.ship("TRACK123", "FedEx");

        CancelOrderCommand command = new CancelOrderCommand(order.getId(), "Reason");

        PaymentPort.PaymentResult refundSuccess = new PaymentPort.PaymentResult(
            true,
            "refund_123",
            "Refund processed"
        );

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentPort.refundPayment(any(), any())).thenReturn(refundSuccess);

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
            .hasMessageContaining("Cannot cancel order in status");
    }
}
