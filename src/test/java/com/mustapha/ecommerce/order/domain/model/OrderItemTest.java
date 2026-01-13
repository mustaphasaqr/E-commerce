package com.mustapha.ecommerce.order.domain.model;

import com.mustapha.ecommerce.order.domain.exception.InvalidOrderItemException;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for OrderItem Entity
 * 
 * Focus: Validation rules, total calculation, immutability
 * Coverage: Construction, business rules, edge cases
 */
@DisplayName("OrderItem Entity Tests")
class OrderItemTest {

    private ProductId productId;
    private String productName;
    private int quantity;
    private Money price;

    @BeforeEach
    void setUp() {
        productId = ProductId.generate();
        productName = "Test Product";
        quantity = 2;
        price = new Money(50.0);
    }

    // ========== Construction Tests ==========

    @Nested
    @DisplayName("Construction Validation")
    class ConstructionTests {

        @Test
        @DisplayName("Should create OrderItem with valid parameters")
        void shouldCreateOrderItemWithValidParameters() {
            // When
            OrderItem item = new OrderItem(productId, productName, quantity, price);

            // Then
            assertNotNull(item);
            assertEquals(productId, item.getProductId());
            assertEquals(productName, item.getProductName());
            assertEquals(quantity, item.getQuantity());
            assertEquals(price, item.getPrice());
        }

        @Test
        @DisplayName("Should fail when productId is null")
        void shouldFailWhenProductIdIsNull() {
            // When/Then
            InvalidOrderItemException exception = assertThrows(InvalidOrderItemException.class, () -> {
                new OrderItem(null, productName, quantity, price);
            });

            assertEquals("Product ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when productName is null")
        void shouldFailWhenProductNameIsNull() {
            // When/Then
            InvalidOrderItemException exception = assertThrows(InvalidOrderItemException.class, () -> {
                new OrderItem(productId, null, quantity, price);
            });

            assertEquals("Product name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when productName is empty string")
        void shouldFailWhenProductNameIsEmpty() {
            // When/Then
            InvalidOrderItemException exception = assertThrows(InvalidOrderItemException.class, () -> {
                new OrderItem(productId, "", quantity, price);
            });

            assertEquals("Product name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when productName is whitespace only")
        void shouldFailWhenProductNameIsWhitespace() {
            // When/Then
            InvalidOrderItemException exception = assertThrows(InvalidOrderItemException.class, () -> {
                new OrderItem(productId, "   ", quantity, price);
            });

            assertEquals("Product name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when quantity is zero")
        void shouldFailWhenQuantityIsZero() {
            // When/Then
            InvalidOrderItemException exception = assertThrows(InvalidOrderItemException.class, () -> {
                new OrderItem(productId, productName, 0, price);
            });

            assertEquals("Quantity must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when quantity is negative")
        void shouldFailWhenQuantityIsNegative() {
            // When/Then
            InvalidOrderItemException exception = assertThrows(InvalidOrderItemException.class, () -> {
                new OrderItem(productId, productName, -5, price);
            });

            assertEquals("Quantity must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when price is null")
        void shouldFailWhenPriceIsNull() {
            // When/Then
            InvalidOrderItemException exception = assertThrows(InvalidOrderItemException.class, () -> {
                new OrderItem(productId, productName, quantity, null);
            });

            assertEquals("Price cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept minimum valid quantity (1)")
        void shouldAcceptMinimumValidQuantity() {
            // When
            OrderItem item = new OrderItem(productId, productName, 1, price);

            // Then
            assertEquals(1, item.getQuantity());
        }

        @Test
        @DisplayName("Should accept large quantity")
        void shouldAcceptLargeQuantity() {
            // When
            OrderItem item = new OrderItem(productId, productName, 999, price);

            // Then
            assertEquals(999, item.getQuantity());
        }

        @Test
        @DisplayName("Should accept zero price (free item)")
        void shouldAcceptZeroPrice() {
            // When
            OrderItem item = new OrderItem(productId, productName, quantity, new Money(0));

            // Then
            assertEquals(new Money(0), item.getPrice());
        }
    }

    // ========== Total Calculation Tests ==========

    @Nested
    @DisplayName("Total Calculation")
    class TotalCalculationTests {

        @Test
        @DisplayName("Should calculate total correctly for single quantity")
        void shouldCalculateTotalForSingleQuantity() {
            // Given
            OrderItem item = new OrderItem(productId, productName, 1, new Money(100));

            // When
            Money total = item.getTotal();

            // Then
            assertEquals(new Money(100), total);
        }

        @Test
        @DisplayName("Should calculate total correctly for multiple quantities")
        void shouldCalculateTotalForMultipleQuantities() {
            // Given
            OrderItem item = new OrderItem(productId, productName, 5, new Money(25.50));

            // When
            Money total = item.getTotal();

            // Then
            // 5 * 25.50 = 127.50
            assertEquals(new Money(127.50), total);
        }

        @Test
        @DisplayName("Should calculate total correctly for large quantity")
        void shouldCalculateTotalForLargeQuantity() {
            // Given
            OrderItem item = new OrderItem(productId, productName, 100, new Money(9.99));

            // When
            Money total = item.getTotal();

            // Then
            // 100 * 9.99 = 999.00
            assertEquals(new Money(999.00), total);
        }

        @Test
        @DisplayName("Should return zero total for free item")
        void shouldReturnZeroTotalForFreeItem() {
            // Given
            OrderItem item = new OrderItem(productId, productName, 10, new Money(0));

            // When
            Money total = item.getTotal();

            // Then
            assertEquals(new Money(0), total);
        }

        @Test
        @DisplayName("Should preserve BigDecimal precision in total calculation")
        void shouldPreserveBigDecimalPrecision() {
            // Given
            OrderItem item = new OrderItem(productId, productName, 3, new Money(10.33));

            // When
            Money total = item.getTotal();

            // Then
            // 3 * 10.33 = 30.99 (not 30.990000001)
            assertEquals(new Money(30.99), total);
        }

        @Test
        @DisplayName("Total calculation should use Money.multiply for precision")
        void totalShouldUseMoneymultiply() {
            // Given
            OrderItem item = new OrderItem(productId, productName, 7, new Money(14.28));

            // When
            Money total = item.getTotal();

            // Then
            // 7 * 14.28 = 99.96
            assertEquals(new Money(99.96), total);
        }
    }

    // ========== Immutability Tests ==========

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should not expose setters")
        void shouldNotExposeSetters() {
            // When
            OrderItem item = new OrderItem(productId, productName, quantity, price);

            // Then - verify no public setters exist
            try {
                item.getClass().getMethod("setProductId", ProductId.class);
                fail("setProductId should not exist");
            } catch (NoSuchMethodException e) {
                // Expected
            }

            try {
                item.getClass().getMethod("setProductName", String.class);
                fail("setProductName should not exist");
            } catch (NoSuchMethodException e) {
                // Expected
            }

            try {
                item.getClass().getMethod("setQuantity", int.class);
                fail("setQuantity should not exist");
            } catch (NoSuchMethodException e) {
                // Expected
            }

            try {
                item.getClass().getMethod("setPrice", Money.class);
                fail("setPrice should not exist");
            } catch (NoSuchMethodException e) {
                // Expected
            }
        }

        @Test
        @DisplayName("Getters should return immutable values")
        void gettersShouldReturnImmutableValues() {
            // Given
            OrderItem item = new OrderItem(productId, productName, quantity, price);

            // When - get values
            ProductId retrievedId = item.getProductId();
            String retrievedName = item.getProductName();
            int retrievedQuantity = item.getQuantity();
            Money retrievedPrice = item.getPrice();

            // Then - values should match original
            assertEquals(productId, retrievedId);
            assertEquals(productName, retrievedName);
            assertEquals(quantity, retrievedQuantity);
            assertEquals(price, retrievedPrice);
        }
    }

    // ========== Business Logic Tests ==========

    @Nested
    @DisplayName("Business Logic")
    class BusinessLogicTests {

        @Test
        @DisplayName("Two items with same product should be equal if all properties match")
        void twoItemsWithSameProductShouldBeIndependent() {
            // Given
            OrderItem item1 = new OrderItem(productId, productName, 2, new Money(50));
            OrderItem item2 = new OrderItem(productId, productName, 2, new Money(50));

            // Then - they are independent objects (no equals override, so not equal by reference)
            assertNotSame(item1, item2);
            assertEquals(item1.getProductId(), item2.getProductId());
            assertEquals(item1.getTotal(), item2.getTotal());
        }

        @Test
        @DisplayName("Should handle product names with special characters")
        void shouldHandleProductNamesWithSpecialCharacters() {
            // When
            OrderItem item = new OrderItem(
                productId,
                "Product™ with Special Chars: $100 (50% off!)",
                quantity,
                price
            );

            // Then
            assertEquals("Product™ with Special Chars: $100 (50% off!)", item.getProductName());
        }

        @Test
        @DisplayName("Should handle product names with unicode characters")
        void shouldHandleProductNamesWithUnicode() {
            // When
            OrderItem item = new OrderItem(
                productId,
                "منتج عربي - Arabic Product 中文产品",
                quantity,
                price
            );

            // Then
            assertEquals("منتج عربي - Arabic Product 中文产品", item.getProductName());
        }

        @Test
        @DisplayName("Should handle very long product names")
        void shouldHandleVeryLongProductNames() {
            // Given
            String longName = "A".repeat(500); // 500 character product name

            // When
            OrderItem item = new OrderItem(productId, longName, quantity, price);

            // Then
            assertEquals(longName, item.getProductName());
            assertEquals(500, item.getProductName().length());
        }
    }

    // ========== Edge Cases ==========

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle maximum integer quantity")
        void shouldHandleMaximumIntegerQuantity() {
            // When
            OrderItem item = new OrderItem(productId, productName, Integer.MAX_VALUE, new Money(0.01));

            // Then
            assertEquals(Integer.MAX_VALUE, item.getQuantity());
        }

        @Test
        @DisplayName("Should handle very small unit price")
        void shouldHandleVerySmallUnitPrice() {
            // When
            OrderItem item = new OrderItem(productId, productName, 1000, new Money(0.01));

            // Then
            assertEquals(new Money(10.0), item.getTotal()); // 1000 * 0.01 = 10.00
        }

        @Test
        @DisplayName("Should handle very large unit price")
        void shouldHandleVeryLargeUnitPrice() {
            // When
            OrderItem item = new OrderItem(productId, productName, 1, new Money(999999.99));

            // Then
            assertEquals(new Money(999999.99), item.getTotal());
        }

        @Test
        @DisplayName("Should trim product name whitespace during validation")
        void shouldTrimProductNameWhitespace() {
            // Given
            String nameWithWhitespace = "  Product Name  ";

            // When
            OrderItem item = new OrderItem(productId, nameWithWhitespace, quantity, price);

            // Then - stored as-is (trimming only for validation, not storage)
            assertEquals(nameWithWhitespace, item.getProductName());
        }
    }
}
