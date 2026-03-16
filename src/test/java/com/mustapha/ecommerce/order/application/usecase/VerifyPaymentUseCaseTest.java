package com.mustapha.ecommerce.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.application.command.VerifyPaymentCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentStatus;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentVerificationResult;
import com.mustapha.ecommerce.order.application.port.ProductPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

@DisplayName("VerifyPaymentUseCase")
class VerifyPaymentUseCaseTest {

    private OrderRepository orderRepository;
    private PaymentPort paymentPort;
    private ProductPort productPort;
    private DomainEventPublisher eventPublisher;
    private NotificationPort notificationPort;
    private VerifyPaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentPort = mock(PaymentPort.class);
        productPort = mock(ProductPort.class);
        eventPublisher = mock(DomainEventPublisher.class);
        notificationPort = mock(NotificationPort.class);

        useCase = new VerifyPaymentUseCase(
            orderRepository,
            paymentPort,
            productPort,
            eventPublisher,
            notificationPort
        );
    }

    private Order createConfirmedOrder() {
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(
                new ProductId("product-1"),
                "Product 1",
                1,
                new Money(50.0)
            ))
            .addItem(new OrderItem(
                new ProductId("product-2"),
                "Product 2",
                2,
                new Money(25.0)
            ))
            .build();
        order.confirm();
        return order;
    }

    @Nested
    @DisplayName("Success Flow")
    class SuccessFlow {

        @Test
        @DisplayName("Should mark order as paid and fulfill reservation exactly once per item")
        void shouldMarkPaidAndFulfillReservation() {
            // Given
            Order order = createConfirmedOrder();
            String checkoutId = "checkout-success-1";
            VerifyPaymentCommand command = new VerifyPaymentCommand(checkoutId);
            PaymentVerificationResult gatewayResult = new PaymentVerificationResult(
                true,
                "txn-1001",
                PaymentStatus.SUCCESS,
                "Payment verified"
            );

            when(paymentPort.verifyPayment(checkoutId)).thenReturn(gatewayResult);
            when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentVerificationResult result = useCase.execute(command);

            // Then
            assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getTransactionId()).isEqualTo("txn-1001");

            verify(productPort, times(1)).fulfillReservation(new ProductId("product-1"), order.getId().getValue());
            verify(productPort, times(1)).fulfillReservation(new ProductId("product-2"), order.getId().getValue());
            verify(productPort, never()).releaseReservation(any(), any());

            verify(orderRepository, times(1)).save(order);
            verify(eventPublisher, atLeastOnce()).publish(any());
            verify(notificationPort, times(1)).sendPaymentReceived(order.getCustomerId(), order.getId());
        }

        @Test
        @DisplayName("Should not fulfill reservation again on repeated SUCCESS verification")
        void shouldNotFulfillTwiceOnRepeatedSuccessVerification() {
            // Given
            Order order = createConfirmedOrder();
            String checkoutId = "checkout-success-repeat";
            VerifyPaymentCommand command = new VerifyPaymentCommand(checkoutId);
            PaymentVerificationResult gatewayResult = new PaymentVerificationResult(
                true,
                "txn-1002",
                PaymentStatus.SUCCESS,
                "Payment verified"
            );

            when(paymentPort.verifyPayment(checkoutId)).thenReturn(gatewayResult);
            when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            useCase.execute(command);

            // Then
            assertThatThrownBy(() -> useCase.execute(command))
                .hasMessageContaining("Cannot pay twice");

            verify(paymentPort, times(2)).verifyPayment(checkoutId);
            verify(productPort, times(1)).fulfillReservation(new ProductId("product-1"), order.getId().getValue());
            verify(productPort, times(1)).fulfillReservation(new ProductId("product-2"), order.getId().getValue());
            verify(orderRepository, times(1)).save(order);
        }
    }

    @Nested
    @DisplayName("Failure and Cancellation Flows")
    class FailureAndCancellationFlows {

        @Test
        @DisplayName("Should release reservation when payment fails")
        void shouldReleaseReservationOnFailedPayment() {
            // Given
            Order order = createConfirmedOrder();
            String checkoutId = "checkout-failed-1";
            VerifyPaymentCommand command = new VerifyPaymentCommand(checkoutId);
            PaymentVerificationResult gatewayResult = new PaymentVerificationResult(
                false,
                "txn-failed-1",
                PaymentStatus.FAILED,
                "Card declined"
            );

            when(paymentPort.verifyPayment(checkoutId)).thenReturn(gatewayResult);
            when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(order));

            // When
            PaymentVerificationResult result = useCase.execute(command);

            // Then
            assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            verify(productPort, times(1)).releaseReservation(new ProductId("product-1"), order.getId().getValue());
            verify(productPort, times(1)).releaseReservation(new ProductId("product-2"), order.getId().getValue());
            verify(productPort, never()).fulfillReservation(any(), any());

            verify(orderRepository, never()).save(any());
            verify(notificationPort, never()).sendPaymentReceived(any(), any());
        }

        @Test
        @DisplayName("Should release reservation when payment is cancelled")
        void shouldReleaseReservationOnCancelledPayment() {
            // Given
            Order order = createConfirmedOrder();
            String checkoutId = "checkout-cancelled-1";
            VerifyPaymentCommand command = new VerifyPaymentCommand(checkoutId);
            PaymentVerificationResult gatewayResult = new PaymentVerificationResult(
                false,
                null,
                PaymentStatus.CANCELLED,
                "Customer cancelled"
            );

            when(paymentPort.verifyPayment(checkoutId)).thenReturn(gatewayResult);
            when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(order));

            // When
            PaymentVerificationResult result = useCase.execute(command);

            // Then
            assertThat(result.status()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            verify(productPort, times(1)).releaseReservation(new ProductId("product-1"), order.getId().getValue());
            verify(productPort, times(1)).releaseReservation(new ProductId("product-2"), order.getId().getValue());
            verify(productPort, never()).fulfillReservation(any(), any());

            verify(orderRepository, never()).save(any());
            verify(notificationPort, never()).sendPaymentReceived(any(), any());
        }
    }

    @Test
    @DisplayName("Should throw when checkoutId does not map to any order")
    void shouldThrowWhenOrderNotFoundForCheckout() {
        // Given
        String checkoutId = "checkout-missing";
        VerifyPaymentCommand command = new VerifyPaymentCommand(checkoutId);
        PaymentVerificationResult gatewayResult = new PaymentVerificationResult(
            true,
            "txn-missing",
            PaymentStatus.SUCCESS,
            "Payment verified"
        );

        when(paymentPort.verifyPayment(checkoutId)).thenReturn(gatewayResult);
        when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No order found for checkoutId");

        verify(orderRepository, never()).save(any());
        verify(productPort, never()).fulfillReservation(any(), any());
        verify(productPort, never()).releaseReservation(any(), any());
    }
}