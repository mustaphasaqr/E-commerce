package com.mustapha.ecommerce.order.domain.service;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SeasonalDiscount Domain Service Tests
 * 
 * Focus: Strategy pattern implementation, discount calculations, validation
 * Reviewer: "Pure domain - no Spring annotations!"
 */
@DisplayName("SeasonalDiscount Strategy Tests")
class SeasonalDiscountTest {

    private CustomerId customerId;
    private ProductId productId;

    @BeforeEach
    void setUp() {
        customerId = CustomerId.generate();
        productId = ProductId.generate();
    }

    @Nested
    @DisplayName("Construction and Validation")
    class ConstructionTests {
        
        @Test
        @DisplayName("Should create SeasonalDiscount with default rate (10%)")
        void shouldCreateWithDefaultRate() {
            // When
            SeasonalDiscount discount = new SeasonalDiscount();
            
            // Then
            assertNotNull(discount);
        }
        
        @Test
        @DisplayName("Should create SeasonalDiscount with custom rate")
        void shouldCreateWithCustomRate() {
            // When
            SeasonalDiscount discount = new SeasonalDiscount(0.25);
            
            // Then
            assertNotNull(discount);
        }
        
        @Test
        @DisplayName("Should fail when discount rate is negative")
        void shouldFailWhenRateIsNegative() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new SeasonalDiscount(-0.10);
            }, "Discount rate must be between 0 and 1");
        }
        
        @Test
        @DisplayName("Should fail when discount rate exceeds 1")
        void shouldFailWhenRateExceedsOne() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new SeasonalDiscount(1.5);
            }, "Discount rate must be between 0 and 1");
        }
        
        @Test
        @DisplayName("Should accept discount rate of 0 (no discount)")
        void shouldAcceptRateOfZero() {
            // When
            SeasonalDiscount discount = new SeasonalDiscount(0.0);
            
            // Then
            assertNotNull(discount);
        }
        
        @Test
        @DisplayName("Should accept discount rate of 1 (100% discount)")
        void shouldAcceptRateOfOne() {
            // When
            SeasonalDiscount discount = new SeasonalDiscount(1.0);
            
            // Then
            assertNotNull(discount);
        }
        
        @Test
        @DisplayName("Should accept valid mid-range rates")
        void shouldAcceptValidMidRangeRates() {
            // When/Then
            assertDoesNotThrow(() -> new SeasonalDiscount(0.05));
            assertDoesNotThrow(() -> new SeasonalDiscount(0.15));
            assertDoesNotThrow(() -> new SeasonalDiscount(0.25));
            assertDoesNotThrow(() -> new SeasonalDiscount(0.50));
            assertDoesNotThrow(() -> new SeasonalDiscount(0.75));
        }
    }

    @Nested
    @DisplayName("Apply Discount - Calculations")
    class ApplyDiscountTests {
        
        @Test
        @DisplayName("Should apply 10% discount correctly")
        void shouldApply10PercentDiscount() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.10);
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 100 - (100 * 0.10) = 90.00
            assertEquals(new Money(90.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should apply 25% discount correctly")
        void shouldApply25PercentDiscount() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.25);
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 100 - (100 * 0.25) = 75.00
            assertEquals(new Money(75.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should apply 50% discount correctly")
        void shouldApply50PercentDiscount() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.50);
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 100 - (100 * 0.50) = 50.00
            assertEquals(new Money(50.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should apply 100% discount (free)")
        void shouldApply100PercentDiscount() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(1.0);
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 100 - (100 * 1.0) = 0.00
            assertEquals(new Money(0.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should apply 0% discount (no discount)")
        void shouldApplyZeroPercentDiscount() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.0);
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 100 - (100 * 0.0) = 100.00
            assertEquals(new Money(100.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should handle small amounts")
        void shouldHandleSmallAmounts() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.10);
            Order order = createOrderWithTotal(5.0);
            Money basePrice = new Money(5.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 5 - (5 * 0.10) = 4.50
            assertEquals(new Money(4.50), finalPrice);
        }
        
        @Test
        @DisplayName("Should handle large amounts")
        void shouldHandleLargeAmounts() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.15);
            Order order = createOrderWithTotal(10000.0);
            Money basePrice = new Money(10000.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 10000 - (10000 * 0.15) = 8500.00
            assertEquals(new Money(8500.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should handle decimal discount rates")
        void shouldHandleDecimalDiscountRates() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.123); // 12.3%
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 100 - (100 * 0.123) = 87.70
            assertEquals(new Money(87.70), finalPrice);
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {
        
        @Test
        @DisplayName("Should fail when order is null")
        void shouldFailWhenOrderIsNull() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.10);
            Money basePrice = new Money(100.0);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                discount.applyDiscount(null, basePrice);
            }, "Order cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when base price is null")
        void shouldFailWhenBasePriceIsNull() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.10);
            Order order = createOrderWithTotal(100.0);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                discount.applyDiscount(order, null);
            }, "Base price cannot be null");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle zero base price")
        void shouldHandleZeroBasePrice() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.10);
            Order order = createOrderWithTotal(0.0);
            Money basePrice = new Money(0.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 0 - (0 * 0.10) = 0.00
            assertEquals(new Money(0.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should round result correctly")
        void shouldRoundResultCorrectly() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.333); // 33.3%
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 100 - (100 * 0.333) = 66.70 (rounded)
            assertEquals(new Money(66.70), finalPrice);
        }
        
        @Test
        @DisplayName("Should work with default constructor (10% discount)")
        void shouldWorkWithDefaultConstructor() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(); // Default 10%
            Order order = createOrderWithTotal(200.0);
            Money basePrice = new Money(200.0);
            
            // When
            Money finalPrice = discount.applyDiscount(order, basePrice);
            
            // Then - 200 - (200 * 0.10) = 180.00
            assertEquals(new Money(180.0), finalPrice);
        }
    }

    @Nested
    @DisplayName("Strategy Pattern Behavior")
    class StrategyPatternTests {
        
        @Test
        @DisplayName("Should implement DiscountPolicy interface")
        void shouldImplementDiscountPolicyInterface() {
            // Given
            SeasonalDiscount discount = new SeasonalDiscount(0.10);
            
            // When/Then
            assertTrue(discount instanceof DiscountPolicy);
        }
        
        @Test
        @DisplayName("Should be interchangeable with other DiscountPolicy implementations")
        void shouldBeInterchangeable() {
            // Given
            DiscountPolicy policy1 = new SeasonalDiscount(0.10);
            DiscountPolicy policy2 = new SeasonalDiscount(0.20);
            Order order = createOrderWithTotal(100.0);
            Money basePrice = new Money(100.0);
            
            // When
            Money price1 = policy1.applyDiscount(order, basePrice);
            Money price2 = policy2.applyDiscount(order, basePrice);
            
            // Then - Different strategies, different results
            assertEquals(new Money(90.0), price1);
            assertEquals(new Money(80.0), price2);
        }
    }

    // ========== Helper Methods ==========
    
    private Order createOrderWithTotal(double total) {
        OrderItem item = OrderItem.create(productId, "Test Product", 1, new Money(total));
        return new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item
                        "quantity", 1,
                        "price", total
                    )
                ))
                .build();
    }
}
