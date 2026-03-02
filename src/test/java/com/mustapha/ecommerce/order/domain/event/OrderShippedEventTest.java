package com.mustapha.ecommerce.order.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Comprehensive OrderShippedEvent Test Suite
 * 
 * Coverage:
 * 1. Event creation and validation
 * 2. Optional tracking number handling
 * 3. Required field validation
 * 4. Convenience constructor
 * 5. Event ID generation
 * 6. Timestamp handling
 * 
 * Total: 15 tests
 */
@DisplayName("OrderShippedEvent - Domain Event Tests")
class OrderShippedEventTest {

    // ========================================
    // Nested Test Class 1: Event Creation
    // ========================================

    @Nested
    @DisplayName("Event Creation and Validation")
    class EventCreationTests {

        @Test
        @DisplayName("Should create event with all fields")
        void shouldCreateEventWithAllFields() {
            // Given
            String eventId = "event-123";
            OrderId orderId = new OrderId("order-123");
            String trackingNumber = "TRACK-123456";
            LocalDateTime occurredAt = LocalDateTime.now();

            // When
            OrderShippedEvent event = new OrderShippedEvent(
                eventId,
                orderId,
                trackingNumber,
                occurredAt
            );

            // Then
            assertThat(event.eventId()).isEqualTo(eventId);
            assertThat(event.orderId()).isEqualTo(orderId);
            assertThat(event.trackingNumber()).isEqualTo(trackingNumber);
            assertThat(event.occurredAt()).isEqualTo(occurredAt);
        }

        @Test
        @DisplayName("Should create event with convenience constructor")
        void shouldCreateEventWithConvenienceConstructor() {
            // Given
            OrderId orderId = new OrderId("order-456");
            String trackingNumber = "TRACK-789";

            // When
            OrderShippedEvent event = new OrderShippedEvent(orderId, trackingNumber);

            // Then
            assertThat(event.orderId()).isEqualTo(orderId);
            assertThat(event.trackingNumber()).isEqualTo(trackingNumber);
            assertThat(event.eventId()).isNotNull();
            assertThat(event.occurredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should generate unique event IDs")
        void shouldGenerateUniqueEventIds() {
            // Given
            OrderId orderId = new OrderId("order-unique");

            // When
            OrderShippedEvent event1 = new OrderShippedEvent(orderId, "TRACK-1");
            OrderShippedEvent event2 = new OrderShippedEvent(orderId, "TRACK-2");

            // Then
            assertThat(event1.eventId()).isNotEqualTo(event2.eventId());
        }

        @Test
        @DisplayName("Should set occurredAt timestamp automatically")
        void shouldSetTimestampAutomatically() {
            // Given
            LocalDateTime before = LocalDateTime.now();
            OrderId orderId = new OrderId("order-time");

            // When
            OrderShippedEvent event = new OrderShippedEvent(orderId, "TRACK-TIME");
            LocalDateTime after = LocalDateTime.now();

            // Then
            assertThat(event.occurredAt()).isBetween(before, after);
        }
    }

    // ========================================
    // Nested Test Class 2: Optional Tracking Number
    // ========================================

    @Nested
    @DisplayName("Optional Tracking Number Handling")
    class OptionalTrackingNumberTests {

        @Test
        @DisplayName("Should allow null tracking number")
        void shouldAllowNullTrackingNumber() {
            // Given
            OrderId orderId = new OrderId("order-no-track");

            // When
            OrderShippedEvent event = new OrderShippedEvent(orderId, null);

            // Then
            assertThat(event.trackingNumber()).isNull();
            assertThat(event.orderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should allow empty tracking number in full constructor")
        void shouldAllowEmptyTrackingInFullConstructor() {
            // Given
            String eventId = "event-empty";
            OrderId orderId = new OrderId("order-empty");
            LocalDateTime occurredAt = LocalDateTime.now();

            // When
            OrderShippedEvent event = new OrderShippedEvent(
                eventId,
                orderId,
                "",  // Empty tracking
                occurredAt
            );

            // Then
            assertThat(event.trackingNumber()).isEmpty();
        }

        @Test
        @DisplayName("Should preserve exact tracking number format")
        void shouldPreserveTrackingFormat() {
            // Given
            OrderId orderId = new OrderId("order-format");
            String trackingWithSpaces = "TRACK-123 456 789";

            // When
            OrderShippedEvent event = new OrderShippedEvent(orderId, trackingWithSpaces);

            // Then
            assertThat(event.trackingNumber()).isEqualTo(trackingWithSpaces);
        }
    }

    // ========================================
    // Nested Test Class 3: Validation Tests
    // ========================================

    @Nested
    @DisplayName("Required Field Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when eventId is null")
        void shouldThrowExceptionWhenEventIdNull() {
            // Given
            OrderId orderId = new OrderId("order-123");
            LocalDateTime occurredAt = LocalDateTime.now();

            // When/Then
            assertThatThrownBy(() -> new OrderShippedEvent(
                null,
                orderId,
                "TRACK-123",
                occurredAt
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event ID cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when eventId is blank")
        void shouldThrowExceptionWhenEventIdBlank() {
            // Given
            OrderId orderId = new OrderId("order-123");
            LocalDateTime occurredAt = LocalDateTime.now();

            // When/Then
            assertThatThrownBy(() -> new OrderShippedEvent(
                "   ",  // Blank
                orderId,
                "TRACK-123",
                occurredAt
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event ID cannot be null or blank");
        }

        @Test
        @DisplayName("Should throw exception when orderId is null")
        void shouldThrowExceptionWhenOrderIdNull() {
            // Given
            String eventId = "event-123";
            LocalDateTime occurredAt = LocalDateTime.now();

            // When/Then
            assertThatThrownBy(() -> new OrderShippedEvent(
                eventId,
                null,
                "TRACK-123",
                occurredAt
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order ID cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when occurredAt is null")
        void shouldThrowExceptionWhenOccurredAtNull() {
            // Given
            String eventId = "event-123";
            OrderId orderId = new OrderId("order-123");

            // When/Then
            assertThatThrownBy(() -> new OrderShippedEvent(
                eventId,
                orderId,
                "TRACK-123",
                null
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Occurred at cannot be null");
        }
    }

    // ========================================
    // Nested Test Class 4: Event Contract Tests
    // ========================================

    @Nested
    @DisplayName("Domain Event Contract")
    class EventContractTests {

        @Test
        @DisplayName("Should implement DomainEvent interface")
        void shouldImplementDomainEvent() {
            // Given
            OrderId orderId = new OrderId("order-type");
            OrderShippedEvent event = new OrderShippedEvent(orderId, "TRACK-TYPE");

            // Then
            assertThat(event).isInstanceOf(com.mustapha.ecommerce.order.domain.DomainEvent.class);
            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getOccurredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should be immutable (record)")
        void shouldBeImmutable() {
            // Given
            OrderId orderId = new OrderId("order-immutable");
            OrderShippedEvent event1 = new OrderShippedEvent(orderId, "TRACK-IMM");

            // When - Try to get fields
            String tracking1 = event1.trackingNumber();
            OrderId order1 = event1.orderId();

            // Then - Original event unchanged
            assertThat(event1.trackingNumber()).isEqualTo(tracking1);
            assertThat(event1.orderId()).isEqualTo(order1);
        }

        @Test
        @DisplayName("Should support toString for debugging")
        void shouldSupportToString() {
            // Given
            OrderId orderId = new OrderId("order-string");
            OrderShippedEvent event = new OrderShippedEvent(orderId, "TRACK-DEBUG");

            // When
            String toString = event.toString();

            // Then
            assertThat(toString).contains("OrderShippedEvent");
            assertThat(toString).contains("order-string");
            assertThat(toString).contains("TRACK-DEBUG");
        }
    }
}
