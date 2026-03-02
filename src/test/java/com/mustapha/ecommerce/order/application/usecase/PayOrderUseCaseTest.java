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

import com.mustapha.ecommerce.order.application.command.PayOrderCommand;
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

/**
 * Test PayOrderUseCase
 * Pattern: Unit Test with Mocks
 * Coverage: Happy path, error cases, edge cases
 */
@DisplayName("PayOrderUseCase")
class PayOrderUseCaseTest {

    private OrderRepository orderRepository;
    private PaymentPort paymentPort;
    private DomainEventPublisher eventPublisher;
    private NotificationPort notificationPort;
    private PayOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentPort = mock(PaymentPort.class);
        eventPublisher = mock(DomainEventPublisher.class);
        notificationPort = mock(NotificationPort.class);
        useCase = new PayOrderUseCase(orderRepository, paymentPort, eventPublisher, notificationPort);
    }

    private Order createConfirmedOrder() {
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
        return order;
    }

    @Nested
    @DisplayName("Successful Payment")
    class SuccessfulPayment {

        @Test
        @DisplayName("Should process payment and mark order as paid")
        void shouldProcessPaymentSuccessfully() {
            // Given
            Order order = createConfirmedOrder();
            PayOrderCommand command = new PayOrderCommand(
                order.getId(),
                "credit_card",
                "tok_visa",
                new Money(100.0)
            );

            PaymentPort.CheckoutResult successResult = new PaymentPort.CheckoutResult(
                true,
                "txn_123",
                "https://example.com/checkout",
                3600,
                "Payment successful"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(paymentPort.createCheckout(
                order.getId(),
                command.getAmount(),
                command.getPaymentMethod(),
                "customer-123@example.com"
            )).thenReturn(successResult);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = useCase.execute(command);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(paymentPort).createCheckout(
                order.getId(),
                command.getAmount(),
                "credit_card",
                "customer-123@example.com"
            );
            verify(orderRepository).save(order);
            verify(notificationPort).sendPaymentReceived(order.getCustomerId(), order.getId());
        }

        @Test
        @DisplayName("Should publish domain events after payment")
        void shouldPublishDomainEvents() {
            // Given
            Order order = createConfirmedOrder();
            PayOrderCommand command = new PayOrderCommand(
                order.getId(),
                "credit_card",
                "tok_visa",
                new Money(100.0)
            );

            PaymentPort.CheckoutResult successResult = new PaymentPort.CheckoutResult(
                true,
                "txn_123",
                "https://example.com/checkout",
                3600,
                "Payment successful"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(paymentPort.createCheckout(any(), any(), any(), any())).thenReturn(successResult);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            useCase.execute(command);

            // Then - Events were published and cleared
            verify(eventPublisher).publish(any());
            assertThat(order.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Payment Failures")
    class PaymentFailures {

        @Test
        @DisplayName("Should throw exception when payment fails")
        void shouldThrowExceptionWhenPaymentFails() {
            // Given
            Order order = createConfirmedOrder();
            PayOrderCommand command = new PayOrderCommand(
                order.getId(),
                "credit_card",
                "tok_invalid",
                new Money(100.0)
            );

            PaymentPort.CheckoutResult failureResult = new PaymentPort.CheckoutResult(
                false,
                null,
                null,
                0,
                "Insufficient funds"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(paymentPort.createCheckout(any(), any(), any(), any())).thenReturn(failureResult);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment checkout failed");

            // Order should not be saved or marked as paid
            verify(orderRepository, never()).save(any());
            verify(notificationPort, never()).sendPaymentReceived(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            OrderId nonExistentId = new OrderId("non-existent");
            PayOrderCommand command = new PayOrderCommand(
                nonExistentId,
                "credit_card",
                "tok_visa",
                new Money(100.0)
            );

            when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");

            verify(paymentPort, never()).createCheckout(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Invalid State Transitions")
    class InvalidStateTransitions {

        @Test
        @DisplayName("Should throw exception when order is already paid")
        void shouldThrowExceptionWhenAlreadyPaid() {
            // Given
            Order order = createConfirmedOrder();
            order.markAsPaid(); // Already paid

            PayOrderCommand command = new PayOrderCommand(
                order.getId(),
                "credit_card",
                "tok_visa",
                new Money(100.0)
            );

            PaymentPort.CheckoutResult successResult = new PaymentPort.CheckoutResult(
                true,
                "txn_123",
                "https://example.com/checkout",
                3600,
                "Payment successful"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(paymentPort.createCheckout(any(), any(), any(), any())).thenReturn(successResult);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .hasMessageContaining("Cannot pay twice");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when order is not confirmed")
        void shouldThrowExceptionWhenNotConfirmed() {
            // Given - Order in PENDING state
            Order order = new OrderBuilder()
                .withCustomerId(new CustomerId("customer-123"))
                .addItem(new OrderItem(
                    new ProductId("product-1"),
                    "Test Product",
                    1,
                    new Money(50.0)
                ))
                .build();
            // Don't confirm the order

            PayOrderCommand command = new PayOrderCommand(
                order.getId(),
                "credit_card",
                "tok_visa",
                new Money(50.0)
            );

            PaymentPort.CheckoutResult successResult = new PaymentPort.CheckoutResult(
                true,
                "txn_123",
                "https://example.com/checkout",
                3600,
                "Payment successful"
            );

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(paymentPort.createCheckout(any(), any(), any(), any())).thenReturn(successResult);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .hasMessageContaining("Cannot transition to PAID");
        }
    }
}
