package com.mustapha.ecommerce.order.domain.event;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrderCancelledEvent Test - Domain Event Validation Tests
 * 
 * Test Coverage:
 * - Event creation with all fields
 * - Convenience constructor (auto-generated ID and timestamp)
 * - Required field validation (eventId, orderId, items, occurredAt)
 * - Optional field handling (reason can be null)
 * - DomainEvent interface compliance
 * - Record functionality
 * - Items list validation
 * 
 * Production Risk: MEDIUM
 * - Invalid events = stock not released, inventory corruption
 * - Event structure must be correct for downstream listeners
 */
@DisplayName("OrderCancelledEvent - Domain Event Tests")
class OrderCancelledEventTest {

    // ========================================
    // Nested Test Class 1: Event Creation
    // ========================================

    @Nested
    @DisplayName("Event Creation with All Fields")
    class EventCreationTests {

        @Test
        @DisplayName("Should create event with all fields including reason")
        void shouldCreateEventWithAllFields() {
            // Given
            String eventId = "event-123";
            OrderId orderId = new OrderId("order-456");
            List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto("product-1", 2),
                new OrderItemDto("product-2", 5)
            );
            String reason = "Customer requested cancellation";
            LocalDateTime occurredAt = LocalDateTime.now();

            // When
            OrderCancelledEvent event = new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                reason,
                occurredAt
            );

            // Then
            assertThat(event.eventId()).isEqualTo(eventId);
            assertThat(event.orderId()).isEqualTo(orderId);
            assertThat(event.items()).isEqualTo(items);
            assertThat(event.reason()).isEqualTo(reason);
            assertThat(event.occurredAt()).isEqualTo(occurredAt);
        }

        @Test
        @DisplayName("Should create event with null reason (optional)")
        void shouldCreateEventWithNullReason() {
            // Given
            String eventId = "event-789";
            OrderId orderId = new OrderId("order-101");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 1)
            );
            String reason = null;  // Reason is optional
            LocalDateTime occurredAt = LocalDateTime.now();

            // When
            OrderCancelledEvent event = new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                reason,
                occurredAt
            );

            // Then
            assertThat(event.reason()).isNull();
            assertThat(event.eventId()).isNotNull();
            assertThat(event.orderId()).isNotNull();
            assertThat(event.items()).isNotEmpty();
        }

        @Test
        @DisplayName("Should create event using convenience constructor")
        void shouldCreateEventUsingConvenienceConstructor() {
            // Given
            OrderId orderId = new OrderId("order-202");
            List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto("product-A", 3),
                new OrderItemDto("product-B", 1)
            );
            String reason = "Payment failed";

            // When
            OrderCancelledEvent event = new OrderCancelledEvent(
                orderId,
                items,
                reason
            );

            // Then
            assertThat(event.eventId()).isNotNull();
            assertThat(event.eventId()).isNotBlank();
            assertThat(event.orderId()).isEqualTo(orderId);
            assertThat(event.items()).isEqualTo(items);
            assertThat(event.reason()).isEqualTo(reason);
            assertThat(event.occurredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should generate unique event IDs with convenience constructor")
        void shouldGenerateUniqueEventIds() {
            // Given
            OrderId orderId = new OrderId("order-303");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 1)
            );

            // When
            OrderCancelledEvent event1 = new OrderCancelledEvent(orderId, items, "Reason 1");
            OrderCancelledEvent event2 = new OrderCancelledEvent(orderId, items, "Reason 2");

            // Then
            assertThat(event1.eventId()).isNotEqualTo(event2.eventId());
        }
    }

    // ========================================
    // Nested Test Class 2: Required Field Validation
    // ========================================

    @Nested
    @DisplayName("Required Field Validation")
    class RequiredFieldValidationTests {

        @Test
        @DisplayName("Should reject null event ID")
        void shouldRejectNullEventId() {
            // Given
            String eventId = null;
            OrderId orderId = new OrderId("order-404");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 1)
            );

            // When & Then
            assertThatThrownBy(() -> new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                "Test",
                LocalDateTime.now()
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event ID cannot be null or blank");
        }

        @Test
        @DisplayName("Should reject blank event ID")
        void shouldRejectBlankEventId() {
            // Given
            String eventId = "   ";
            OrderId orderId = new OrderId("order-505");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 1)
            );

            // When & Then
            assertThatThrownBy(() -> new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                "Test",
                LocalDateTime.now()
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event ID cannot be null or blank");
        }

        @Test
        @DisplayName("Should reject null order ID")
        void shouldRejectNullOrderId() {
            // Given
            String eventId = "event-606";
            OrderId orderId = null;
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 1)
            );

            // When & Then
            assertThatThrownBy(() -> new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                "Test",
                LocalDateTime.now()
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order ID cannot be null");
        }

        @Test
        @DisplayName("Should reject null items list")
        void shouldRejectNullItemsList() {
            // Given
            String eventId = "event-707";
            OrderId orderId = new OrderId("order-707");
            List<OrderItemDto> items = null;

            // When & Then
            assertThatThrownBy(() -> new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                "Test",
                LocalDateTime.now()
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Items list cannot be null or empty");
        }

        @Test
        @DisplayName("Should reject empty items list")
        void shouldRejectEmptyItemsList() {
            // Given
            String eventId = "event-808";
            OrderId orderId = new OrderId("order-808");
            List<OrderItemDto> items = Collections.emptyList();

            // When & Then
            assertThatThrownBy(() -> new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                "Test",
                LocalDateTime.now()
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Items list cannot be null or empty");
        }

        @Test
        @DisplayName("Should reject null occurred at timestamp")
        void shouldRejectNullOccurredAt() {
            // Given
            String eventId = "event-909";
            OrderId orderId = new OrderId("order-909");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 1)
            );
            LocalDateTime occurredAt = null;

            // When & Then
            assertThatThrownBy(() -> new OrderCancelledEvent(
                eventId,
                orderId,
                items,
                "Test",
                occurredAt
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Occurred at cannot be null");
        }
    }

    // ========================================
    // Nested Test Class 3: Items List Validation
    // ========================================

    @Nested
    @DisplayName("Items List Validation")
    class ItemsListValidationTests {

        @Test
        @DisplayName("Should accept single item in list")
        void shouldAcceptSingleItem() {
            // Given
            OrderId orderId = new OrderId("order-single");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 5)
            );

            // When
            OrderCancelledEvent event = new OrderCancelledEvent(
                orderId,
                items,
                "Single item test"
            );

            // Then
            assertThat(event.items()).hasSize(1);
            assertThat(event.items().get(0).productId()).isEqualTo("product-1");
        }

        @Test
        @DisplayName("Should accept multiple items in list")
        void shouldAcceptMultipleItems() {
            // Given
            OrderId orderId = new OrderId("order-multiple");
            List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto("product-1", 2),
                new OrderItemDto("product-2", 3),
                new OrderItemDto("product-3", 1)
            );

            // When
            OrderCancelledEvent event = new OrderCancelledEvent(
                orderId,
                items,
                "Multiple items test"
            );

            // Then
            assertThat(event.items()).hasSize(3);
            assertThat(event.items()).containsExactlyElementsOf(items);
        }

        @Test
        @DisplayName("Should preserve items order in list")
        void shouldPreserveItemsOrder() {
            // Given
            OrderId orderId = new OrderId("order-order");
            OrderItemDto item1 = new OrderItemDto("product-A", 1);
            OrderItemDto item2 = new OrderItemDto("product-B", 2);
            OrderItemDto item3 = new OrderItemDto("product-C", 3);
            List<OrderItemDto> items = Arrays.asList(item1, item2, item3);

            // When
            OrderCancelledEvent event = new OrderCancelledEvent(
                orderId,
                items,
                "Order test"
            );

            // Then
            assertThat(event.items().get(0)).isEqualTo(item1);
            assertThat(event.items().get(1)).isEqualTo(item2);
            assertThat(event.items().get(2)).isEqualTo(item3);
        }

        @Test
        @DisplayName("Should handle large items list")
        void shouldHandleLargeItemsList() {
            // Given
            OrderId orderId = new OrderId("order-bulk");
            List<OrderItemDto> items = new java.util.ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                items.add(new OrderItemDto("product-" + i, i));
            }

            // When
            OrderCancelledEvent event = new OrderCancelledEvent(
                orderId,
                items,
                "Bulk order cancelled"
            );

            // Then
            assertThat(event.items()).hasSize(100);
        }
    }

    // ========================================
    // Nested Test Class 4: DomainEvent Interface
    // ========================================

    @Nested
    @DisplayName("DomainEvent Interface Compliance")
    class DomainEventInterfaceTests {

        @Test
        @DisplayName("Should implement getEventId from DomainEvent interface")
        void shouldImplementGetEventId() {
            // Given
            String expectedEventId = "event-interface-test";
            OrderCancelledEvent event = new OrderCancelledEvent(
                expectedEventId,
                new OrderId("order-123"),
                List.of(new OrderItemDto("product-1", 1)),
                "Interface test",
                LocalDateTime.now()
            );

            // When
            String eventId = event.getEventId();

            // Then
            assertThat(eventId).isEqualTo(expectedEventId);
        }

        @Test
        @DisplayName("Should implement getOccurredAt from DomainEvent interface")
        void shouldImplementGetOccurredAt() {
            // Given
            LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 3, 4, 10, 30);
            OrderCancelledEvent event = new OrderCancelledEvent(
                "event-123",
                new OrderId("order-456"),
                List.of(new OrderItemDto("product-1", 1)),
                "Interface test",
                expectedTimestamp
            );

            // When
            LocalDateTime occurredAt = event.getOccurredAt();

            // Then
            assertThat(occurredAt).isEqualTo(expectedTimestamp);
        }
    }

    // ========================================
    // Nested Test Class 5: Record Functionality
    // ========================================

    @Nested
    @DisplayName("Record Functionality Tests")
    class RecordFunctionalityTests {

        @Test
        @DisplayName("Should provide equality by value")
        void shouldProvideEqualityByValue() {
            // Given
            String eventId = "event-eq-1";
            OrderId orderId = new OrderId("order-eq");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 5)
            );
            String reason = "Equality test";
            LocalDateTime timestamp = LocalDateTime.of(2026, 3, 4, 12, 0);

            OrderCancelledEvent event1 = new OrderCancelledEvent(
                eventId, orderId, items, reason, timestamp
            );
            OrderCancelledEvent event2 = new OrderCancelledEvent(
                eventId, orderId, items, reason, timestamp
            );

            // When & Then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different event ID")
        void shouldNotBeEqualWithDifferentEventId() {
            // Given
            OrderId orderId = new OrderId("order-123");
            List<OrderItemDto> items = List.of(
                new OrderItemDto("product-1", 1)
            );
            LocalDateTime timestamp = LocalDateTime.now();

            OrderCancelledEvent event1 = new OrderCancelledEvent(
                "event-1", orderId, items, "Test", timestamp
            );
            OrderCancelledEvent event2 = new OrderCancelledEvent(
                "event-2", orderId, items, "Test", timestamp
            );

            // When & Then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("Should provide toString with key information")
        void shouldProvideToStringWithKeyInfo() {
            // Given
            OrderCancelledEvent event = new OrderCancelledEvent(
                "event-toString",
                new OrderId("order-toString"),
                List.of(new OrderItemDto("product-1", 3)),
                "ToString test",
                LocalDateTime.now()
            );

            // When
            String toString = event.toString();

            // Then
            assertThat(toString).contains("event-toString");
            assertThat(toString).contains("order-toString");
        }
    }

    // ========================================
    // Nested Test Class 6: Edge Cases
    // ========================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long reason text")
        void shouldHandleVeryLongReason() {
            // Given
            String longReason = "a".repeat(5000);
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-long-reason"),
                List.of(new OrderItemDto("product-1", 1)),
                longReason
            );

            // When & Then
            assertThat(event.reason()).hasSize(5000);
        }

        @Test
        @DisplayName("Should handle reason with special characters")
        void shouldHandleReasonWithSpecialCharacters() {
            // Given
            String specialReason = "Cancelled due to: <>\"'&%$#@!";
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-special"),
                List.of(new OrderItemDto("product-1", 1)),
                specialReason
            );

            // When & Then
            assertThat(event.reason()).isEqualTo(specialReason);
        }

        @Test
        @DisplayName("Should handle multiline reason")
        void shouldHandleMultilineReason() {
            // Given
            String multilineReason = "Line 1: Customer request\nLine 2: Out of stock\nLine 3: Refund issued";
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-multiline"),
                List.of(new OrderItemDto("product-1", 1)),
                multilineReason
            );

            // When & Then
            assertThat(event.reason()).contains("\n");
            assertThat(event.reason()).isEqualTo(multilineReason);
        }

        @Test
        @DisplayName("Should handle past timestamp")
        void shouldHandlePastTimestamp() {
            // Given
            LocalDateTime pastTimestamp = LocalDateTime.of(2020, 1, 1, 0, 0);
            OrderCancelledEvent event = new OrderCancelledEvent(
                "event-past",
                new OrderId("order-past"),
                List.of(new OrderItemDto("product-1", 1)),
                "Past event",
                pastTimestamp
            );

            // When & Then
            assertThat(event.occurredAt()).isEqualTo(pastTimestamp);
        }

        @Test
        @DisplayName("Should handle future timestamp (edge case)")
        void shouldHandleFutureTimestamp() {
            // Given
            LocalDateTime futureTimestamp = LocalDateTime.of(2030, 12, 31, 23, 59);
            OrderCancelledEvent event = new OrderCancelledEvent(
                "event-future",
                new OrderId("order-future"),
                List.of(new OrderItemDto("product-1", 1)),
                "Future event",
                futureTimestamp
            );

            // When & Then
            assertThat(event.occurredAt()).isEqualTo(futureTimestamp);
        }

        @Test
        @DisplayName("Should handle Unicode characters in reason")
        void shouldHandleUnicodeCharactersInReason() {
            // Given
            String unicodeReason = "Cancelled: 取消订单 🚫 отменено";
            OrderCancelledEvent event = new OrderCancelledEvent(
                new OrderId("order-unicode"),
                List.of(new OrderItemDto("product-1", 1)),
                unicodeReason
            );

            // When & Then
            assertThat(event.reason()).isEqualTo(unicodeReason);
        }
    }
}
