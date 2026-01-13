package com.mustapha.ecommerce.order.domain.service;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
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
 * PricingService Domain Service Tests
 * 
 * Focus: Discount calculations, validation, cross-aggregate logic
 * Reviewer: "Domain Services = pure domain, no Spring!"
 */
@DisplayName("PricingService Domain Service Tests")
class PricingServiceTest {

    private PricingService pricingService;
    private DiscountPolicy discountPolicy;
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
        @DisplayName("Should create PricingService with valid discount policy")
        void shouldCreatePricingServiceWithValidPolicy() {
            // Given
            DiscountPolicy policy = new SeasonalDiscount(0.10);
            
            // When
            PricingService service = new PricingService(policy);
            
            // Then
            assertNotNull(service);
        }
        
        @Test
        @DisplayName("Should fail when discount policy is null")
        void shouldFailWhenDiscountPolicyIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new PricingService(null);
            }, "Discount policy cannot be null");
        }
    }

    @Nested
    @DisplayName("Calculate Final Price")
    class CalculateFinalPriceTests {
        
        @BeforeEach
        void setUp() {
            // Use 10% discount for tests
            discountPolicy = new SeasonalDiscount(0.10);
            pricingService = new PricingService(discountPolicy);
        }
        
        @Test
        @DisplayName("Should calculate final price with 10% discount")
        void shouldCalculateFinalPriceWithDiscount() {
            // Given - Order with total 100.00
            Order order = createOrderWithTotal(100.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then - 100 - (100 * 0.10) = 90.00
            assertEquals(new Money(90.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should calculate final price with 20% discount")
        void shouldCalculateFinalPriceWith20PercentDiscount() {
            // Given
            discountPolicy = new SeasonalDiscount(0.20);
            pricingService = new PricingService(discountPolicy);
            Order order = createOrderWithTotal(100.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then - 100 - (100 * 0.20) = 80.00
            assertEquals(new Money(80.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should handle zero discount")
        void shouldHandleZeroDiscount() {
            // Given
            discountPolicy = new SeasonalDiscount(0.0); // No discount
            pricingService = new PricingService(discountPolicy);
            Order order = createOrderWithTotal(100.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then - No discount applied
            assertEquals(new Money(100.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should handle 100% discount")
        void shouldHandle100PercentDiscount() {
            // Given
            discountPolicy = new SeasonalDiscount(1.0); // 100% discount
            pricingService = new PricingService(discountPolicy);
            Order order = createOrderWithTotal(100.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then - Free!
            assertEquals(new Money(0.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should fail when order is null")
        void shouldFailWhenOrderIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                pricingService.calculateFinalPrice(null);
            }, "Order cannot be null");
        }
        
        @Test
        @DisplayName("Should handle small order amounts")
        void shouldHandleSmallOrderAmounts() {
            // Given
            Order order = createOrderWithTotal(5.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then - 5 - (5 * 0.10) = 4.50
            assertEquals(new Money(4.50), finalPrice);
        }
        
        @Test
        @DisplayName("Should handle large order amounts")
        void shouldHandleLargeOrderAmounts() {
            // Given
            Order order = createOrderWithTotal(10000.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then - 10000 - (10000 * 0.10) = 9000.00
            assertEquals(new Money(9000.0), finalPrice);
        }
    }

    @Nested
    @DisplayName("Integration with Different Discount Policies")
    class DiscountPolicyIntegrationTests {
        
        @Test
        @DisplayName("Should work with default seasonal discount (10%)")
        void shouldWorkWithDefaultSeasonalDiscount() {
            // Given
            discountPolicy = new SeasonalDiscount(); // Default 10%
            pricingService = new PricingService(discountPolicy);
            Order order = createOrderWithTotal(100.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then
            assertEquals(new Money(90.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should work with custom discount policy")
        void shouldWorkWithCustomDiscountPolicy() {
            // Given - Custom policy that gives 50% discount
            discountPolicy = new DiscountPolicy() {
                @Override
                public Money applyDiscount(Order order, Money basePrice) {
                    return basePrice.multiply(0.5); // 50% of base price
                }
            };
            pricingService = new PricingService(discountPolicy);
            Order order = createOrderWithTotal(100.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then
            assertEquals(new Money(50.0), finalPrice);
        }
        
        @Test
        @DisplayName("Should work with no discount policy")
        void shouldWorkWithNoDiscountPolicy() {
            // Given - Policy that returns base price unchanged
            discountPolicy = new DiscountPolicy() {
                @Override
                public Money applyDiscount(Order order, Money basePrice) {
                    return basePrice;
                }
            };
            pricingService = new PricingService(discountPolicy);
            Order order = createOrderWithTotal(100.0);
            
            // When
            Money finalPrice = pricingService.calculateFinalPrice(order);
            
            // Then
            assertEquals(new Money(100.0), finalPrice);
        }
    }

    @Nested
    @DisplayName("Business Rules Validation")
    class BusinessRulesTests {
        
        @BeforeEach
        void setUp() {
            discountPolicy = new SeasonalDiscount(0.10);
            pricingService = new PricingService(discountPolicy);
        }
        
        @Test
        @DisplayName("Should prevent negative final price")
        void shouldPreventNegativeFinalPrice() {
            // Given - Malicious discount policy that returns negative
            discountPolicy = new DiscountPolicy() {
                @Override
                public Money applyDiscount(Order order, Money basePrice) {
                    // Try to return negative price
                    return new Money(0.0).subtract(new Money(10.0)); // This will throw in Money
                }
            };
            pricingService = new PricingService(discountPolicy);
            Order order = createOrderWithTotal(100.0);
            
            // When/Then - Should fail at Money level
            assertThrows(IllegalArgumentException.class, () -> {
                pricingService.calculateFinalPrice(order);
            });
        }
    }

    // ========== Helper Methods ==========
    
    private Order createOrderWithTotal(double total) {
        return new OrderBuilder()
                .withCustomerId(customerId.getValue())
                .withItems(List.of(
                    java.util.Map.of(
                        "productId", productId.getValue(),
                        "productName", "Test Product",
                        "quantity", 1,
                        "price", total
                    )
                ))
                .build();
    }
}
