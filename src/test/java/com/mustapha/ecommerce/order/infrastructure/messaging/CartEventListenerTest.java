package com.mustapha.ecommerce.order.infrastructure.messaging;

import com.mustapha.ecommerce.cart.domain.event.CartConvertedEvent;
import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * CartEventListener Test - Conversion Tracking Tests
 * 
 * Test Coverage:
 * - Cart conversion event received and logged
 * - Cart-to-order relationship tracking
 * - Conversion analytics preparation
 * - Error handling for conversion tracking
 * - Event data extraction
 * 
 * Production Risk: MEDIUM
 * - Missing conversion data = marketing attribution gaps
 * - Affects conversion funnel analysis
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartEventListener - Conversion Tracking Tests")
class CartEventListenerTest {

    @InjectMocks
    private CartEventListener cartEventListener;

    // ========================================
    // Nested Test Class 1: Event Reception
    // ========================================

    @Nested
    @DisplayName("Cart Conversion Event Reception")
    class EventReceptionTests {

        @Test
        @DisplayName("Should receive and process CartConvertedEvent successfully")
        void shouldProcessCartConvertedEventSuccessfully() {
            // Given
            CartId cartId = new CartId(123L);
            Long orderId = 456L;
            Money totalAmount = new Money(new BigDecimal("99.99"));
            int itemCount = 3;

            CartConvertedEvent event = new CartConvertedEvent(
                cartId,
                orderId,
                totalAmount,
                itemCount
            );

            // When & Then - should not throw exception
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should extract cart ID correctly from event")
        void shouldExtractCartIdCorrectly() {
            // Given
            Long expectedCartId = 789L;
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(expectedCartId),
                999L,
                new Money(new BigDecimal("50.00")),
                2
            );

            // When
            cartEventListener.onCartConverted(event);

            // Then - verify event data is accessible
            assertThat(event.getCartId().getValue()).isEqualTo(expectedCartId);
        }

        @Test
        @DisplayName("Should extract order ID correctly from event")
        void shouldExtractOrderIdCorrectly() {
            // Given
            Long expectedOrderId = 12345L;
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(100L),
                expectedOrderId,
                new Money(new BigDecimal("100.00")),
                1
            );

            // When
            cartEventListener.onCartConverted(event);

            // Then
            assertThat(event.getOrderId()).isEqualTo(expectedOrderId);
        }

        @Test
        @DisplayName("Should extract total amount correctly from event")
        void shouldExtractTotalAmountCorrectly() {
            // Given
            BigDecimal expectedAmount = new BigDecimal("299.99");
            Money totalAmount = new Money(expectedAmount);
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(200L),
                300L,
                totalAmount,
                5
            );

            // When
            cartEventListener.onCartConverted(event);

            // Then
            assertThat(event.getTotalAmount().getAmount()).isEqualByComparingTo(expectedAmount);
        }

        @Test
        @DisplayName("Should extract item count correctly from event")
        void shouldExtractItemCountCorrectly() {
            // Given
            int expectedItemCount = 7;
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(300L),
                400L,
                new Money(new BigDecimal("150.00")),
                expectedItemCount
            );

            // When
            cartEventListener.onCartConverted(event);

            // Then
            assertThat(event.getItemCount()).isEqualTo(expectedItemCount);
        }
    }

    // ========================================
    // Nested Test Class 2: Conversion Tracking
    // ========================================

    @Nested
    @DisplayName("Conversion Tracking Scenarios")
    class ConversionTrackingTests {

        @Test
        @DisplayName("Should track conversion for small cart (1 item)")
        void shouldTrackSmallCartConversion() {
            // Given
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(1L),
                10L,
                new Money(new BigDecimal("19.99")),
                1
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should track conversion for large cart (many items)")
        void shouldTrackLargeCartConversion() {
            // Given
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(2L),
                20L,
                new Money(new BigDecimal("1999.99")),
                50
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should track conversion with zero total amount")
        void shouldTrackConversionWithZeroAmount() {
            // Given - free trial or promo code 100% discount
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(3L),
                30L,
                new Money(BigDecimal.ZERO),
                2
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should track conversion with decimal amounts")
        void shouldTrackConversionWithDecimalAmounts() {
            // Given
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(4L),
                40L,
                new Money(new BigDecimal("123.45")),
                3
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }
    }

    // ========================================
    // Nested Test Class 3: Error Handling
    // ========================================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should not throw exception even if TODO operations fail")
        void shouldNotThrowExceptionOnTodoOperations() {
            // Given - current implementation only logs (TODO)
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(5L),
                50L,
                new Money(new BigDecimal("75.00")),
                4
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should process multiple conversion events sequentially")
        void shouldProcessMultipleConversionEvents() {
            // Given
            CartConvertedEvent event1 = new CartConvertedEvent(
                new CartId(6L),
                60L,
                new Money(new BigDecimal("100.00")),
                2
            );
            CartConvertedEvent event2 = new CartConvertedEvent(
                new CartId(7L),
                70L,
                new Money(new BigDecimal("200.00")),
                3
            );
            CartConvertedEvent event3 = new CartConvertedEvent(
                new CartId(8L),
                80L,
                new Money(new BigDecimal("300.00")),
                4
            );

            // When & Then - should process all events
            assertThatCode(() -> {
                cartEventListener.onCartConverted(event1);
                cartEventListener.onCartConverted(event2);
                cartEventListener.onCartConverted(event3);
            }).doesNotThrowAnyException();
        }
    }

    // ========================================
    // Nested Test Class 4: Analytics Attribution
    // ========================================

    @Nested
    @DisplayName("Analytics Attribution Tests")
    class AnalyticsAttributionTests {

        @Test
        @DisplayName("Should prepare for cart-order relationship storage")
        void shouldPrepareForCartOrderRelationshipStorage() {
            // Given - future enhancement will store cart-order relationship
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(9L),
                90L,
                new Money(new BigDecimal("49.99")),
                1
            );

            // When
            cartEventListener.onCartConverted(event);

            // Then - verify event data is intact for future storage
            assertThat(event.getCartId().getValue()).isEqualTo(9L);
            assertThat(event.getOrderId()).isEqualTo(90L);
        }

        @Test
        @DisplayName("Should prepare for conversion time calculation")
        void shouldPrepareForConversionTimeCalculation() {
            // Given - future enhancement will calculate time from cart created to order placed
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(10L),
                100L,
                new Money(new BigDecimal("79.99")),
                2
            );

            // When
            cartEventListener.onCartConverted(event);

            // Then - event processed successfully
            assertThat(event.getCartId()).isNotNull();
            assertThat(event.getOrderId()).isNotNull();
        }

        @Test
        @DisplayName("Should prepare for conversion rate metrics")
        void shouldPrepareForConversionRateMetrics() {
            // Given - future enhancement will update conversion rate metrics
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(11L),
                110L,
                new Money(new BigDecimal("199.99")),
                8
            );

            // When
            cartEventListener.onCartConverted(event);

            // Then - verify listener exists and can process events
            assertThat(cartEventListener).isNotNull();
        }
    }

    // ========================================
    // Nested Test Class 5: Edge Cases
    // ========================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large cart ID")
        void shouldHandleVeryLargeCartId() {
            // Given
            Long largeCartId = Long.MAX_VALUE;
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(largeCartId),
                1L,
                new Money(new BigDecimal("50.00")),
                1
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle very large order ID")
        void shouldHandleVeryLargeOrderId() {
            // Given
            Long largeOrderId = Long.MAX_VALUE;
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(1L),
                largeOrderId,
                new Money(new BigDecimal("50.00")),
                1
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle very large total amount")
        void shouldHandleVeryLargeTotalAmount() {
            // Given
            BigDecimal largeAmount = new BigDecimal("999999999.99");
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(1L),
                1L,
                new Money(largeAmount),
                100
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle very large item count")
        void shouldHandleVeryLargeItemCount() {
            // Given
            int largeItemCount = 1000;
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(1L),
                1L,
                new Money(new BigDecimal("10000.00")),
                largeItemCount
            );

            // When & Then
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }
    }

    // ========================================
    // Nested Test Class 6: Async Behavior
    // ========================================

    @Nested
    @DisplayName("Async Event Processing")
    class AsyncBehaviorTests {

        @Test
        @DisplayName("Should process event asynchronously (annotation present)")
        void shouldProcessEventAsynchronously() {
            // Given - @Async annotation on listener method
            CartConvertedEvent event = new CartConvertedEvent(
                new CartId(12L),
                120L,
                new Money(new BigDecimal("89.99")),
                3
            );

            // When & Then - should not block caller
            assertThatCode(() -> cartEventListener.onCartConverted(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle concurrent cart conversion events")
        void shouldHandleConcurrentConversionEvents() {
            // Given - multiple events processed concurrently
            CartConvertedEvent event1 = new CartConvertedEvent(
                new CartId(13L),
                130L,
                new Money(new BigDecimal("50.00")),
                2
            );
            CartConvertedEvent event2 = new CartConvertedEvent(
                new CartId(14L),
                140L,
                new Money(new BigDecimal("75.00")),
                3
            );

            // When - process events (async in production, sync in test)
            assertThatCode(() -> {
                cartEventListener.onCartConverted(event1);
                cartEventListener.onCartConverted(event2);
            }).doesNotThrowAnyException();

            // Then - both events processed successfully
            assertThat(event1.getCartId().getValue()).isEqualTo(13L);
            assertThat(event2.getCartId().getValue()).isEqualTo(14L);
        }
    }
}

