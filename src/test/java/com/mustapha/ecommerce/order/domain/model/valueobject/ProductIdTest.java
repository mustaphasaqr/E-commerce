package com.mustapha.ecommerce.order.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductId Value Object Tests
 * 
 * Focus: Type safety, validation, immutability, factory methods
 * Reviewer: "Prevents mixing product IDs with customer/order IDs!"
 */
@DisplayName("ProductId Value Object Tests")
class ProductIdTest {

    @Nested
    @DisplayName("Construction and Validation")
    class ConstructionTests {
        
        @Test
        @DisplayName("Should create ProductId with valid value")
        void shouldCreateProductIdWithValidValue() {
            // When
            ProductId productId = new ProductId("product-123");
            
            // Then
            assertNotNull(productId);
            assertEquals("product-123", productId.getValue());
        }
        
        @Test
        @DisplayName("Should fail when value is null")
        void shouldFailWhenValueIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new ProductId(null);
            }, "Product ID cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when value is blank")
        void shouldFailWhenValueIsBlank() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new ProductId("   ");
            }, "Product ID cannot be blank");
        }
        
        @Test
        @DisplayName("Should fail when value is empty")
        void shouldFailWhenValueIsEmpty() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new ProductId("");
            }, "Product ID cannot be empty");
        }
        
        @Test
        @DisplayName("Should fail when value exceeds max length")
        void shouldFailWhenValueExceedsMaxLength() {
            // Given
            String tooLong = "x".repeat(101);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new ProductId(tooLong);
            }, "Product ID cannot exceed 100 characters");
        }
        
        @Test
        @DisplayName("Should accept value at max length (100 chars)")
        void shouldAcceptValueAtMaxLength() {
            // Given
            String maxLength = "x".repeat(100);
            
            // When
            ProductId productId = new ProductId(maxLength);
            
            // Then
            assertNotNull(productId);
            assertEquals(100, productId.getValue().length());
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {
        
        @Test
        @DisplayName("Should generate new ProductId with UUID")
        void shouldGenerateNewProductId() {
            // When
            ProductId productId = ProductId.generate();
            
            // Then
            assertNotNull(productId);
            assertNotNull(productId.getValue());
            assertTrue(productId.getValue().startsWith("PROD-"));
        }
        
        @Test
        @DisplayName("Should generate unique IDs each time")
        void shouldGenerateUniqueIds() {
            // When
            ProductId id1 = ProductId.generate();
            ProductId id2 = ProductId.generate();
            
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
            ProductId id1 = new ProductId("product-123");
            ProductId id2 = new ProductId("product-123");
            
            // When/Then
            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            // Given
            ProductId id1 = new ProductId("product-123");
            ProductId id2 = new ProductId("product-456");
            
            // When/Then
            assertNotEquals(id1, id2);
        }
        
        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            ProductId id = new ProductId("product-123");
            
            // When/Then
            assertEquals(id, id);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            ProductId id = new ProductId("product-123");
            
            // When/Then
            assertNotEquals(id, null);
        }
        
        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            ProductId id = new ProductId("product-123");
            String notProductId = "product-123";
            
            // When/Then
            assertNotEquals(id, notProductId);
        }
        
        @Test
        @DisplayName("Type safety - ProductId != CustomerId even with same value")
        void typeSafetyProductIdNotEqualToCustomerId() {
            // Given
            ProductId productId = new ProductId("123");
            CustomerId customerId = new CustomerId("123");
            
            // When/Then - This won't even compile in real code!
            // But let's test they're different types
            assertNotEquals(productId.getClass(), customerId.getClass());
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable - getValue returns same value")
        void shouldBeImmutable() {
            // Given
            ProductId id = new ProductId("product-123");
            
            // When
            String value1 = id.getValue();
            String value2 = id.getValue();
            
            // Then
            assertEquals(value1, value2);
            assertEquals("product-123", value1);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {
        
        @Test
        @DisplayName("Should return value in toString")
        void shouldReturnValueInToString() {
            // Given
            ProductId id = new ProductId("product-123");
            
            // When
            String result = id.toString();
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("product-123"));
        }
    }
}
