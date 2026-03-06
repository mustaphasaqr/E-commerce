package com.mustapha.ecommerce.order.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrderItemDto Test - DTO Validation Tests
 * 
 * Test Coverage:
 * - Valid DTO creation
 * - ProductId validation (non-null, non-blank)
 * - Quantity validation (positive values)
 * - Edge cases (empty strings, zero/negative quantities)
 * - Record functionality (equality, toString)
 * 
 * Production Risk: LOW
 * - Data integrity validation
 * - Prevents invalid event payloads
 */
@DisplayName("OrderItemDto - DTO Validation Tests")
class OrderItemDtoTest {

    // ========================================
    // Nested Test Class 1: Valid DTO Creation
    // ========================================

    @Nested
    @DisplayName("Valid DTO Creation")
    class ValidDtoCreationTests {

        @Test
        @DisplayName("Should create OrderItemDto with valid data")
        void shouldCreateOrderItemDtoWithValidData() {
            // Given
            String productId = "product-123";
            int quantity = 5;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.productId()).isEqualTo(productId);
            assertThat(dto.quantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("Should create DTO with single quantity")
        void shouldCreateDtoWithSingleQuantity() {
            // Given
            String productId = "product-456";
            int quantity = 1;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.quantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should create DTO with large quantity")
        void shouldCreateDtoWithLargeQuantity() {
            // Given
            String productId = "product-bulk";
            int quantity = 1000;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.quantity()).isEqualTo(1000);
        }

        @Test
        @DisplayName("Should create DTO with UUID-format product ID")
        void shouldCreateDtoWithUuidFormatProductId() {
            // Given
            String productId = "123e4567-e89b-12d3-a456-426614174000";
            int quantity = 3;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.productId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("Should create DTO with alphanumeric product ID")
        void shouldCreateDtoWithAlphanumericProductId() {
            // Given
            String productId = "PROD-ABC-123-XYZ";
            int quantity = 2;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.productId()).isEqualTo(productId);
        }
    }

    // ========================================
    // Nested Test Class 2: ProductId Validation
    // ========================================

    @Nested
    @DisplayName("ProductId Validation Tests")
    class ProductIdValidationTests {

        @Test
        @DisplayName("Should reject null product ID")
        void shouldRejectNullProductId() {
            // Given
            String productId = null;
            int quantity = 5;

            // When & Then
            assertThatThrownBy(() -> new OrderItemDto(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID cannot be null or blank");
        }

        @Test
        @DisplayName("Should reject blank product ID")
        void shouldRejectBlankProductId() {
            // Given
            String productId = "   ";  // Only whitespace
            int quantity = 5;

            // When & Then
            assertThatThrownBy(() -> new OrderItemDto(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID cannot be null or blank");
        }

        @Test
        @DisplayName("Should reject empty string product ID")
        void shouldRejectEmptyStringProductId() {
            // Given
            String productId = "";
            int quantity = 5;

            // When & Then
            assertThatThrownBy(() -> new OrderItemDto(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID cannot be null or blank");
        }
    }

    // ========================================
    // Nested Test Class 3: Quantity Validation
    // ========================================

    @Nested
    @DisplayName("Quantity Validation Tests")
    class QuantityValidationTests {

        @Test
        @DisplayName("Should reject zero quantity")
        void shouldRejectZeroQuantity() {
            // Given
            String productId = "product-789";
            int quantity = 0;

            // When & Then
            assertThatThrownBy(() -> new OrderItemDto(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive")
                .hasMessageContaining("got: 0");
        }

        @Test
        @DisplayName("Should reject negative quantity")
        void shouldRejectNegativeQuantity() {
            // Given
            String productId = "product-789";
            int quantity = -5;

            // When & Then
            assertThatThrownBy(() -> new OrderItemDto(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive")
                .hasMessageContaining("got: -5");
        }

        @Test
        @DisplayName("Should reject negative one quantity")
        void shouldRejectNegativeOneQuantity() {
            // Given
            String productId = "product-789";
            int quantity = -1;

            // When & Then
            assertThatThrownBy(() -> new OrderItemDto(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive")
                .hasMessageContaining("got: -1");
        }

        @Test
        @DisplayName("Should accept quantity of 1 (minimum valid)")
        void shouldAcceptQuantityOfOne() {
            // Given
            String productId = "product-min";
            int quantity = 1;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.quantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should accept large quantity (Integer.MAX_VALUE)")
        void shouldAcceptMaxIntegerQuantity() {
            // Given
            String productId = "product-max";
            int quantity = Integer.MAX_VALUE;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.quantity()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    // ========================================
    // Nested Test Class 4: Record Functionality
    // ========================================

    @Nested
    @DisplayName("Record Functionality Tests")
    class RecordFunctionalityTests {

        @Test
        @DisplayName("Should provide equality by value")
        void shouldProvideEqualityByValue() {
            // Given
            OrderItemDto dto1 = new OrderItemDto("product-123", 5);
            OrderItemDto dto2 = new OrderItemDto("product-123", 5);

            // When & Then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different product ID")
        void shouldNotBeEqualWithDifferentProductId() {
            // Given
            OrderItemDto dto1 = new OrderItemDto("product-123", 5);
            OrderItemDto dto2 = new OrderItemDto("product-456", 5);

            // When & Then
            assertThat(dto1).isNotEqualTo(dto2);
        }

        @Test
        @DisplayName("Should not be equal with different quantity")
        void shouldNotBeEqualWithDifferentQuantity() {
            // Given
            OrderItemDto dto1 = new OrderItemDto("product-123", 5);
            OrderItemDto dto2 = new OrderItemDto("product-123", 10);

            // When & Then
            assertThat(dto1).isNotEqualTo(dto2);
        }

        @Test
        @DisplayName("Should provide toString with all fields")
        void shouldProvideToStringWithAllFields() {
            // Given
            String productId = "product-test";
            int quantity = 7;
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // When
            String toString = dto.toString();

            // Then
            assertThat(toString).contains(productId);
            assertThat(toString).contains("7");
        }

        @Test
        @DisplayName("Should be immutable (record)")
        void shouldBeImmutable() {
            // Given
            OrderItemDto dto = new OrderItemDto("product-immutable", 3);

            // When - attempt to modify (not possible with records)
            String originalProductId = dto.productId();
            int originalQuantity = dto.quantity();

            // Then
            assertThat(dto.productId()).isEqualTo(originalProductId);
            assertThat(dto.quantity()).isEqualTo(originalQuantity);
        }
    }

    // ========================================
    // Nested Test Class 5: Edge Cases
    // ========================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle product ID with special characters")
        void shouldHandleProductIdWithSpecialCharacters() {
            // Given
            String productId = "product-123_ABC-xyz";
            int quantity = 2;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.productId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("Should handle very long product ID")
        void shouldHandleVeryLongProductId() {
            // Given
            String productId = "product-" + "x".repeat(200);
            int quantity = 1;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.productId()).hasSize(208); // "product-" + 200 chars
        }

        @Test
        @DisplayName("Should handle product ID with leading/trailing spaces (not trimmed)")
        void shouldHandleProductIdWithSpaces() {
            // Given - spaces are part of the ID (no trimming)
            String productId = " product-123 ";
            int quantity = 1;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then - spaces preserved (isBlank() checks after trim)
            assertThat(dto.productId()).contains("product-123");
        }

        @Test
        @DisplayName("Should create multiple DTOs for same product")
        void shouldCreateMultipleDtosForSameProduct() {
            // Given - same product, different quantities
            String productId = "product-duplicate";

            // When
            OrderItemDto dto1 = new OrderItemDto(productId, 5);
            OrderItemDto dto2 = new OrderItemDto(productId, 10);

            // Then - both DTOs are independent
            assertThat(dto1.quantity()).isEqualTo(5);
            assertThat(dto2.quantity()).isEqualTo(10);
            assertThat(dto1).isNotEqualTo(dto2);
        }

        @Test
        @DisplayName("Should handle numeric string product ID")
        void shouldHandleNumericStringProductId() {
            // Given
            String productId = "12345";
            int quantity = 3;

            // When
            OrderItemDto dto = new OrderItemDto(productId, quantity);

            // Then
            assertThat(dto.productId()).isEqualTo("12345");
        }
    }

    // ========================================
    // Nested Test Class 6: Validation Error Messages
    // ========================================

    @Nested
    @DisplayName("Validation Error Messages")
    class ValidationErrorMessagesTests {

        @Test
        @DisplayName("Should provide clear error message for null product ID")
        void shouldProvideCleatErrorMessageForNullProductId() {
            // When & Then
            assertThatThrownBy(() -> new OrderItemDto(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID cannot be null or blank");
        }

        @Test
        @DisplayName("Should provide clear error message for zero quantity with value")
        void shouldProvideCleatErrorMessageForZeroQuantity() {
            // When & Then
            assertThatThrownBy(() -> new OrderItemDto("product-123", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive")
                .hasMessageContaining("got: 0");
        }

        @Test
        @DisplayName("Should provide clear error message for negative quantity with value")
        void shouldProvideCleatErrorMessageForNegativeQuantity() {
            // When & Then
            assertThatThrownBy(() -> new OrderItemDto("product-123", -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive")
                .hasMessageContaining("got: -10");
        }
    }
}
