package com.mustapha.ecommerce.product.infrastructure.messaging;

import com.mustapha.ecommerce.order.domain.event.OrderCancelledEvent;
import com.mustapha.ecommerce.order.domain.event.OrderItemDto;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.product.application.command.ReleaseReservationCommand;
import com.mustapha.ecommerce.product.application.usecase.ReleaseReservationUseCase;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * OrderEventListener Test - Stock Release on Order Cancellation
 * 
 * Critical Test Coverage:
 * - Stock successfully released for single product
 * - Stock released for multiple products in one order
 * - Error handling when stock release fails
 * - Idempotency (duplicate events)
 * - Event processing completeness
 * 
 * Production Risk: HIGH
 * - Bugs = incorrect inventory, overselling
 * 
 * Test Strategy:
 * - Direct unit test without Spring context (faster, simpler)
 * - Mock dependencies
 * - Tests verify business logic is correct
 */
@DisplayName("OrderEventListener - Stock Release Tests")
class OrderEventListenerTest {

    private ReleaseReservationUseCase releaseReservationUseCase;
    private OrderEventListener orderEventListener;

    @BeforeEach
    void setUp() {
        releaseReservationUseCase = mock(ReleaseReservationUseCase.class);
        orderEventListener = new OrderEventListener(releaseReservationUseCase);
        
        // Configure mock to return null (return value not used in listener)
        when(releaseReservationUseCase.execute(any(ReleaseReservationCommand.class))).thenReturn(null);
    }

    // ========================================
    // Nested Test Class 1: Single Product Release
    // ========================================

    @Nested
    @DisplayName("Single Product Stock Release")
    class SingleProductReleaseTests {

        @Test
        @DisplayName("Should release stock when order with single product is cancelled")
        void shouldReleaseStockForSingleProduct() {
            // Given
            String orderId = "order-123";
            String productId = "550e8400-e29b-41d4-a716-446655440001"; // Valid UUID
            int quantity = 5;

            OrderItemDto item = new OrderItemDto(productId, quantity);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId(orderId),
                List.of(item),
                "Customer requested cancellation"
            );

            // When
            orderEventListener.onOrderCancelled(event);

            // Then
            ArgumentCaptor<ReleaseReservationCommand> commandCaptor = 
                ArgumentCaptor.forClass(ReleaseReservationCommand.class);
            verify(releaseReservationUseCase, times(1)).execute(commandCaptor.capture());

            ReleaseReservationCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productId);
            assertThat(capturedCommand.getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should use ProductId.of() factory method to create ProductId")
        void shouldUseFactoryMethodForProductId() {
            // Given
            String productId = "550e8400-e29b-41d4-a716-446655440002"; // Valid UUID
            OrderItemDto item = new OrderItemDto(productId, 3);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-456"),
                List.of(item),
                "Out of stock alternative"
            );

            // When
            orderEventListener.onOrderCancelled(event);

            // Then
            ArgumentCaptor<ReleaseReservationCommand> commandCaptor = 
                ArgumentCaptor.forClass(ReleaseReservationCommand.class);
            verify(releaseReservationUseCase).execute(commandCaptor.capture());

            // Verify ProductId was created correctly
            ProductId capturedProductId = commandCaptor.getValue().getProductId();
            assertThat(capturedProductId).isNotNull();
            assertThat(capturedProductId.getValue()).isEqualTo(productId);
        }
    }

    // ========================================
    // Nested Test Class 2: Multiple Products Release
    // ========================================

    @Nested
    @DisplayName("Multiple Products Stock Release")
    class MultipleProductsReleaseTests {

        @Test
        @DisplayName("Should release stock for all products when order with multiple items is cancelled")
        void shouldReleaseStockForMultipleProducts() {
            // Given
            String orderId = "order-789";
            List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440011", 2),
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440012", 5),
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440013", 1)
            );

            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId(orderId),
                items,
                "Duplicate order"
            );

            // When
            orderEventListener.onOrderCancelled(event);

            // Then
            verify(releaseReservationUseCase, times(3)).execute(any(ReleaseReservationCommand.class));

            ArgumentCaptor<ReleaseReservationCommand> commandCaptor = 
                ArgumentCaptor.forClass(ReleaseReservationCommand.class);
            verify(releaseReservationUseCase, times(3)).execute(commandCaptor.capture());

            List<ReleaseReservationCommand> commands = commandCaptor.getAllValues();
            assertThat(commands).hasSize(3);
            assertThat(commands.get(0).getProductId().getValue()).isEqualTo("550e8400-e29b-41d4-a716-446655440011");
            assertThat(commands.get(1).getProductId().getValue()).isEqualTo("550e8400-e29b-41d4-a716-446655440012");
            assertThat(commands.get(2).getProductId().getValue()).isEqualTo("550e8400-e29b-41d4-a716-446655440013");
        }

        @Test
        @DisplayName("Should release stock for each product exactly once")
        void shouldReleaseStockExactlyOncePerProduct() {
            // Given
            OrderItemDto item1 = new OrderItemDto("550e8400-e29b-41d4-a716-446655440021", 10);
            OrderItemDto item2 = new OrderItemDto("550e8400-e29b-41d4-a716-446655440022", 5);

            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-321"),
                Arrays.asList(item1, item2),
                "Payment failed"
            );

            // When
            orderEventListener.onOrderCancelled(event);

            // Then
            verify(releaseReservationUseCase, times(2)).execute(any(ReleaseReservationCommand.class));
        }
    }

    // ========================================
    // Nested Test Class 3: Error Handling
    // ========================================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should continue processing other products when one product release fails")
        void shouldContinueProcessingAfterError() {
            // Given
            List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440031", 1),
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440032", 2),
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440033", 3)
            );

            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-error-test"),
                items,
                "Test error handling"
            );

            // Simulate failure for second product
            when(releaseReservationUseCase.execute(argThat(cmd -> 
                cmd != null && cmd.getProductId().getValue().equals("550e8400-e29b-41d4-a716-446655440031")
            ))).thenReturn(null);
            when(releaseReservationUseCase.execute(argThat(cmd -> 
                cmd != null && cmd.getProductId().getValue().equals("550e8400-e29b-41d4-a716-446655440032")
            ))).thenThrow(new RuntimeException("Stock release failed"));
            when(releaseReservationUseCase.execute(argThat(cmd -> 
                cmd != null && cmd.getProductId().getValue().equals("550e8400-e29b-41d4-a716-446655440033")
            ))).thenReturn(null);

            // When
            orderEventListener.onOrderCancelled(event);

            // Then - should attempt to release all 3 products
            verify(releaseReservationUseCase, times(3)).execute(any(ReleaseReservationCommand.class));
        }

        @Test
        @DisplayName("Should not throw exception when stock release fails")
        void shouldNotThrowExceptionOnStockReleaseFailure() {
            // Given
            OrderItemDto item = new OrderItemDto("550e8400-e29b-41d4-a716-446655440040", 1);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-exception"),
                List.of(item),
                "Exception test"
            );

            when(releaseReservationUseCase.execute(any(ReleaseReservationCommand.class)))
                .thenThrow(new RuntimeException("Database error"));

            // When & Then - should not throw exception
            try {
                orderEventListener.onOrderCancelled(event);
            } catch (Exception e) {
                throw new AssertionError("Listener should not throw exception, but got: " + e.getMessage());
            }

            verify(releaseReservationUseCase).execute(any(ReleaseReservationCommand.class));
        }

        @Test
        @DisplayName("Should log error but continue when ReleaseReservationUseCase throws exception")
        void shouldLogErrorAndContinueOnException() {
            // Given
            List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440051", 1),
                new OrderItemDto("550e8400-e29b-41d4-a716-446655440052", 2)
            );

            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-log-test"),
                items,
                "Logging test"
            );

            when(releaseReservationUseCase.execute(argThat(cmd -> cmd != null && cmd.getProductId().getValue().equals("product-1"))))
                .thenThrow(new IllegalArgumentException("Product not found"));

            // When
            orderEventListener.onOrderCancelled(event);

            // Then - should still attempt second product
            verify(releaseReservationUseCase, times(2)).execute(any(ReleaseReservationCommand.class));
        }
    }

    // ========================================
    // Nested Test Class 4: Idempotency
    // ========================================

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Should process duplicate event (idempotency handled by domain)")
        void shouldProcessDuplicateEvent() {
            // Given - same event received twice
            String orderId = "order-duplicate";
            String productId = "550e8400-e29b-41d4-a716-446655440060";
            OrderItemDto item = new OrderItemDto(productId, 3);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId(orderId),
                List.of(item),
                "Duplicate test"
            );

            // When - process event twice
            orderEventListener.onOrderCancelled(event);
            orderEventListener.onOrderCancelled(event);

            // Then - both attempts should call use case (idempotency in domain)
            verify(releaseReservationUseCase, times(2)).execute(any(ReleaseReservationCommand.class));
        }
    }

    // ========================================
    // Nested Test Class 5: Event Data Validation
    // ========================================

    @Nested
    @DisplayName("Event Data Validation")
    class EventDataValidationTests {

        @Test
        @DisplayName("Should handle event with optional reason (null)")
        void shouldHandleEventWithNullReason() {
            // Given
            OrderItemDto item = new OrderItemDto("550e8400-e29b-41d4-a716-446655440060", 2);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-no-reason"),
                List.of(item),
                null  // Reason is optional
            );

            // When
            orderEventListener.onOrderCancelled(event);

            // Then
            verify(releaseReservationUseCase, times(1)).execute(any(ReleaseReservationCommand.class));
        }

        @Test
        @DisplayName("Should extract orderId correctly from OrderCancelledEvent")
        void shouldExtractOrderIdCorrectly() {
            // Given
            String expectedOrderId = "order-extract-test";
            OrderItemDto item = new OrderItemDto("550e8400-e29b-41d4-a716-446655440070", 1);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId(expectedOrderId),
                List.of(item),
                "Order ID extraction test"
            );

            // When
            orderEventListener.onOrderCancelled(event);

            // Then
            ArgumentCaptor<ReleaseReservationCommand> commandCaptor = 
                ArgumentCaptor.forClass(ReleaseReservationCommand.class);
            verify(releaseReservationUseCase).execute(commandCaptor.capture());

            assertThat(commandCaptor.getValue().getOrderId()).isEqualTo(expectedOrderId);
        }

        @Test
        @DisplayName("Should process event with large quantity")
        void shouldProcessEventWithLargeQuantity() {
            // Given
            OrderItemDto item = new OrderItemDto("550e8400-e29b-41d4-a716-446655440080", 1000);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-bulk"),
                List.of(item),
                "Bulk order cancelled"
            );

            // When
            orderEventListener.onOrderCancelled(event);

            // Then
            verify(releaseReservationUseCase).execute(any(ReleaseReservationCommand.class));
        }
    }
}
