package com.mustapha.ecommerce.order.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CustomerId Value Object Tests
 * 
 * Focus: Type safety, validation, immutability, factory methods
 * Reviewer: "Value Objects prevent mixing IDs - compile-time safety!"
 */
@DisplayName("CustomerId Value Object Tests")
class CustomerIdTest {

    @Nested
    @DisplayName("Construction and Validation")
    class ConstructionTests {
        
        @Test
        @DisplayName("Should create CustomerId with valid value")
        void shouldCreateCustomerIdWithValidValue() {
            // When
            CustomerId customerId = new CustomerId("customer-123");
            
            // Then
            assertNotNull(customerId);
            assertEquals("customer-123", customerId.getValue());
        }
        
        @Test
        @DisplayName("Should fail when value is null")
        void shouldFailWhenValueIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new CustomerId(null);
            }, "Customer ID cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when value is blank")
        void shouldFailWhenValueIsBlank() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new CustomerId("   ");
            }, "Customer ID cannot be blank");
        }
        
        @Test
        @DisplayName("Should fail when value is empty")
        void shouldFailWhenValueIsEmpty() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new CustomerId("");
            }, "Customer ID cannot be empty");
        }
        
        @Test
        @DisplayName("Should fail when value exceeds max length")
        void shouldFailWhenValueExceedsMaxLength() {
            // Given - create string longer than 100 characters
            String tooLong = "x".repeat(101);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new CustomerId(tooLong);
            }, "Customer ID cannot exceed 100 characters");
        }
        
        @Test
        @DisplayName("Should accept value at max length (100 chars)")
        void shouldAcceptValueAtMaxLength() {
            // Given
            String maxLength = "x".repeat(100);
            
            // When
            CustomerId customerId = new CustomerId(maxLength);
            
            // Then
            assertNotNull(customerId);
            assertEquals(100, customerId.getValue().length());
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {
        
        @Test
        @DisplayName("Should generate new CustomerId with UUID")
        void shouldGenerateNewCustomerId() {
            // When
            CustomerId customerId = CustomerId.generate();
            
            // Then
            assertNotNull(customerId);
            assertNotNull(customerId.getValue());
            assertTrue(customerId.getValue().startsWith("CUST-"));
        }
        
        @Test
        @DisplayName("Should generate unique IDs each time")
        void shouldGenerateUniqueIds() {
            // When
            CustomerId id1 = CustomerId.generate();
            CustomerId id2 = CustomerId.generate();
            
            // Then
            assertNotEquals(id1, id2);
            assertNotEquals(id1.getValue(), id2.getValue());
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal when values are same")
        void shouldBeEqualWhenValuesAreSame() {
            // Given
            CustomerId id1 = new CustomerId("customer-123");
            CustomerId id2 = new CustomerId("customer-123");
            
            // When/Then
            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            // Given
            CustomerId id1 = new CustomerId("customer-123");
            CustomerId id2 = new CustomerId("customer-456");
            
            // When/Then
            assertNotEquals(id1, id2);
        }
        
        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            CustomerId id = new CustomerId("customer-123");
            
            // When/Then
            assertEquals(id, id);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            CustomerId id = new CustomerId("customer-123");
            
            // When/Then
            assertNotEquals(id, null);
        }
        
        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            CustomerId id = new CustomerId("customer-123");
            String notCustomerId = "customer-123";
            
            // When/Then
            assertNotEquals(id, notCustomerId);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable - getValue returns same value")
        void shouldBeImmutable() {
            // Given
            CustomerId id = new CustomerId("customer-123");
            
            // When
            String value1 = id.getValue();
            String value2 = id.getValue();
            
            // Then
            assertEquals(value1, value2);
            assertEquals("customer-123", value1);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {
        
        @Test
        @DisplayName("Should return value in toString")
        void shouldReturnValueInToString() {
            // Given
            CustomerId id = new CustomerId("customer-123");
            
            // When
            String result = id.toString();
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("customer-123"));
        }
    }
}
