package com.mustapha.ecommerce.order.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderId Value Object Tests
 * 
 * Focus: Type safety, validation, immutability, UUID generation
 * Reviewer: "Type-safe identifiers prevent mixing up IDs!"
 */
@DisplayName("OrderId Value Object Tests")
class OrderIdTest {

    @Nested
    @DisplayName("Construction and Validation")
    class ConstructionTests {
        
        @Test
        @DisplayName("Should create OrderId with valid value")
        void shouldCreateOrderIdWithValidValue() {
            // When
            OrderId orderId = new OrderId("order-123");
            
            // Then
            assertNotNull(orderId);
            assertEquals("order-123", orderId.getValue());
        }
        
        @Test
        @DisplayName("Should fail when value is null")
        void shouldFailWhenValueIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new OrderId(null);
            }, "Order ID cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when value is blank")
        void shouldFailWhenValueIsBlank() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new OrderId("   ");
            }, "Order ID cannot be blank");
        }
        
        @Test
        @DisplayName("Should fail when value is empty")
        void shouldFailWhenValueIsEmpty() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new OrderId("");
            }, "Order ID cannot be empty");
        }
        
        @Test
        @DisplayName("Should fail when value exceeds max length")
        void shouldFailWhenValueExceedsMaxLength() {
            // Given
            String tooLong = "x".repeat(101);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new OrderId(tooLong);
            }, "Order ID length must be between 1 and 100 characters");
        }
        
        @Test
        @DisplayName("Should accept value at max length (100 chars)")
        void shouldAcceptValueAtMaxLength() {
            // Given
            String maxLength = "x".repeat(100);
            
            // When
            OrderId orderId = new OrderId(maxLength);
            
            // Then
            assertNotNull(orderId);
            assertEquals(100, orderId.getValue().length());
        }
        
        @Test
        @DisplayName("Should accept single character value")
        void shouldAcceptSingleCharacterValue() {
            // When
            OrderId orderId = new OrderId("1");
            
            // Then
            assertNotNull(orderId);
            assertEquals("1", orderId.getValue());
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {
        
        @Test
        @DisplayName("Should generate new OrderId with UUID")
        void shouldGenerateNewOrderId() {
            // When
            OrderId orderId = OrderId.generate();
            
            // Then
            assertNotNull(orderId);
            assertNotNull(orderId.getValue());
            // UUID format: 8-4-4-4-12 characters
            assertTrue(orderId.getValue().length() == 36);
        }
        
        @Test
        @DisplayName("Should generate unique IDs each time")
        void shouldGenerateUniqueIds() {
            // When
            OrderId id1 = OrderId.generate();
            OrderId id2 = OrderId.generate();
            
            // Then
            assertNotEquals(id1, id2);
            assertNotEquals(id1.getValue(), id2.getValue());
        }
        
        @Test
        @DisplayName("Should generate valid UUID format")
        void shouldGenerateValidUuidFormat() {
            // When
            OrderId orderId = OrderId.generate();
            
            // Then - UUID format validation
            String value = orderId.getValue();
            String[] parts = value.split("-");
            assertEquals(5, parts.length, "UUID should have 5 parts separated by hyphens");
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal when values are same")
        void shouldBeEqualWhenValuesAreSame() {
            // Given
            OrderId id1 = new OrderId("order-123");
            OrderId id2 = new OrderId("order-123");
            
            // When/Then
            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            // Given
            OrderId id1 = new OrderId("order-123");
            OrderId id2 = new OrderId("order-456");
            
            // When/Then
            assertNotEquals(id1, id2);
        }
        
        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            OrderId id = new OrderId("order-123");
            
            // When/Then
            assertEquals(id, id);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            OrderId id = new OrderId("order-123");
            
            // When/Then
            assertNotEquals(id, null);
        }
        
        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            OrderId id = new OrderId("order-123");
            String notOrderId = "order-123";
            
            // When/Then
            assertNotEquals(id, notOrderId);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable - getValue returns same value")
        void shouldBeImmutable() {
            // Given
            OrderId id = new OrderId("order-123");
            
            // When
            String value1 = id.getValue();
            String value2 = id.getValue();
            
            // Then
            assertEquals(value1, value2);
            assertEquals("order-123", value1);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {
        
        @Test
        @DisplayName("Should return value in toString")
        void shouldReturnValueInToString() {
            // Given
            OrderId id = new OrderId("order-123");
            
            // When
            String result = id.toString();
            
            // Then
            assertEquals("order-123", result);
        }
    }
}
