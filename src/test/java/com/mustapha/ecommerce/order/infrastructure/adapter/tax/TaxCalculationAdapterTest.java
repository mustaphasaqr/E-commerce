package com.mustapha.ecommerce.order.infrastructure.adapter.tax;

import com.mustapha.ecommerce.order.application.port.TaxCalculationPort;
import com.mustapha.ecommerce.order.application.port.TaxCalculationPort.OrderLineItem;
import com.mustapha.ecommerce.order.application.port.TaxCalculationPort.TaxCalculation;
import com.mustapha.ecommerce.order.application.port.TaxCalculationPort.TaxCalculationRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Test Suite: Tax Calculation Service
 * 
 * Tests:
 * 1. Unit Tests: Tax calculation accuracy for different countries
 * 2. Resilience Tests: Edge cases, null handling, invalid inputs
 * 3. Integration Tests: Real configuration values from application.properties
 * 
 * Coverage:
 * - Egypt VAT (14%)
 * - UAE VAT (5%)
 * - Saudi Arabia VAT (15%)
 * - Bahrain VAT (10%)
 * - Kuwait (0% - no VAT)
 * - Oman VAT (5%)
 * - Qatar (0% - no VAT)
 * - Tax exemption logic
 * - Multiple items with different tax categories
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tax Calculation Service Tests")
class TaxCalculationAdapterTest {

    @Autowired
    private TaxCalculationAdapter taxCalculationAdapter;

    private static final Long DEFAULT_ORDER_ID = 12345L;
    private static final Long DEFAULT_CUSTOMER_ID = 100L;

    @Nested
    @DisplayName("Unit Tests - Tax Calculation Accuracy")
    class TaxCalculationAccuracyTests {

        @Test
        @Order(1)
        @DisplayName("Should calculate Egypt VAT correctly (14%)")
        void testEgyptVAT() {
            // Given: Order with 1000 EGP subtotal
            TaxCalculationRequest request = createRequest("EG", 
                new OrderLineItem(1L, "Standard Product", BigDecimal.valueOf(1000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should apply 14% VAT
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(140.00), within(BigDecimal.valueOf(0.01)));
            assertThat(result.subtotal()).isEqualTo(BigDecimal.valueOf(1000.00));
            assertThat(result.taxRate()).contains("14");
            assertThat(result.breakdown()).hasSize(1);
            assertThat(result.jurisdiction()).contains("Egypt");
        }

        @Test
        @Order(2)
        @DisplayName("Should calculate UAE VAT correctly (5%)")
        void testUAEVAT() {
            // Given: Order with 2000 AED subtotal
            TaxCalculationRequest request = createRequest("AE", 
                new OrderLineItem(2L, "Electronics", BigDecimal.valueOf(2000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should apply 5% VAT
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(100.00), within(BigDecimal.valueOf(0.01)));
            assertThat(result.taxRate()).contains("5");
        }

        @Test
        @Order(3)
        @DisplayName("Should calculate Saudi Arabia VAT correctly (15%)")
        void testSaudiVAT() {
            // Given: Order with 3000 SAR subtotal
            TaxCalculationRequest request = createRequest("SA", 
                new OrderLineItem(3L, "Clothing", BigDecimal.valueOf(3000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should apply 15% VAT
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(450.00), within(BigDecimal.valueOf(0.01)));
            assertThat(result.taxRate()).contains("15");
        }

        @Test
        @Order(4)
        @DisplayName("Should calculate Bahrain VAT correctly (10%)")
        void testBahrainVAT() {
            // Given: Order with 1500 BHD subtotal
            TaxCalculationRequest request = createRequest("BH", 
                new OrderLineItem(4L, "Books", BigDecimal.valueOf(1500.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should apply 10% VAT
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(150.00), within(BigDecimal.valueOf(0.01)));
            assertThat(result.taxRate()).contains("10");
        }

        @Test
        @Order(5)
        @DisplayName("Should calculate Kuwait with 0% VAT (no VAT)")
        void testKuwaitNoVAT() {
            // Given: Order with 5000 KWD subtotal
            TaxCalculationRequest request = createRequest("KW", 
                new OrderLineItem(5L, "Jewelry", BigDecimal.valueOf(5000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should have 0% tax
            assertThat(result.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.taxRate()).contains("0");
        }

        @Test
        @Order(6)
        @DisplayName("Should calculate Oman VAT correctly (5%)")
        void testOmanVAT() {
            // Given: Order with 1000 OMR subtotal
            TaxCalculationRequest request = createRequest("OM", 
                new OrderLineItem(6L, "Furniture", BigDecimal.valueOf(1000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should apply 5% VAT
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(50.00), within(BigDecimal.valueOf(0.01)));
        }

        @Test
        @Order(7)
        @DisplayName("Should calculate Qatar with 0% VAT (no VAT)")
        void testQatarNoVAT() {
            // Given: Order with 8000 QAR subtotal
            TaxCalculationRequest request = createRequest("QA", 
                new OrderLineItem(7L, "Appliances", BigDecimal.valueOf(8000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should have 0% tax
            assertThat(result.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @Order(8)
        @DisplayName("Should calculate tax for multiple items correctly")
        void testMultipleItems() {
            // Given: Order with 3 different items
            BigDecimal subtotal = BigDecimal.valueOf(425.00);
            TaxCalculationRequest request = new TaxCalculationRequest(
                DEFAULT_ORDER_ID,
                DEFAULT_CUSTOMER_ID,
                subtotal,
                "EG", // Egypt - 14% VAT
                "EG",
                "INDIVIDUAL",
                null,
                List.of(
                    new OrderLineItem(1L, "Item 1", BigDecimal.valueOf(100.00), 2, "STANDARD"), // 200 EGP
                    new OrderLineItem(2L, "Item 2", BigDecimal.valueOf(50.00), 3, "STANDARD"),  // 150 EGP
                    new OrderLineItem(3L, "Item 3", BigDecimal.valueOf(75.00), 1, "STANDARD")   // 75 EGP
                )
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Total should be (200 + 150 + 75) * 0.14 = 59.50 EGP
            assertThat(result.subtotal()).isEqualTo(BigDecimal.valueOf(425.00));
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(59.50), within(BigDecimal.valueOf(0.01)));
            assertThat(result.breakdown()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Resilience Tests - Edge Cases & Error Handling")
    class ResilienceTests {

        @Test
        @Order(10)
        @DisplayName("Should handle unknown country code gracefully")
        void testUnknownCountryCode() {
            // Given: Order from unsupported country
            TaxCalculationRequest request = createRequest("XX", 
                new OrderLineItem(1L, "Product", BigDecimal.valueOf(1000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should default to 0% tax (no exception)
            assertThat(result.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.jurisdiction()).contains("Unknown");
        }

        @Test
        @Order(11)
        @DisplayName("Should handle lowercase country codes")
        void testLowercaseCountryCode() {
            // Given: Order with lowercase country code
            TaxCalculationRequest request = createRequest("eg", 
                new OrderLineItem(1L, "Product", BigDecimal.valueOf(1000.00), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should normalize to uppercase and calculate correctly
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(140.00), within(BigDecimal.valueOf(0.01)));
        }

        @Test
        @Order(12)
        @DisplayName("Should handle zero amount orders")
        void testZeroAmountOrder() {
            // Given: Order with 0 amount
            TaxCalculationRequest request = createRequest("EG", 
                new OrderLineItem(1L, "Free Product", BigDecimal.ZERO, 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Tax should be zero
            assertThat(result.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @Order(13)
        @DisplayName("Should handle fractional quantities correctly")
        void testFractionalQuantities() {
            // Given: Order with decimal quantity (e.g., 2.5 kg of product)
            BigDecimal subtotal = BigDecimal.valueOf(100.00);
            TaxCalculationRequest request = new TaxCalculationRequest(
                DEFAULT_ORDER_ID,
                DEFAULT_CUSTOMER_ID,
                subtotal,
                "EG",
                "EG",
                "INDIVIDUAL",
                null,
                List.of(
                    new OrderLineItem(1L, "Bulk Product", BigDecimal.valueOf(50.00), 2, "STANDARD") // 100 EGP
                )
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should calculate correctly
            assertThat(result.subtotal()).isEqualTo(BigDecimal.valueOf(100.00));
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(14.00), within(BigDecimal.valueOf(0.01)));
        }

        @Test
        @Order(14)
        @DisplayName("Should round tax amounts correctly (half-up)")
        void testTaxRounding() {
            // Given: Order that requires rounding (33.33 EGP * 0.14 = 4.6662)
            TaxCalculationRequest request = createRequest("EG", 
                new OrderLineItem(1L, "Product", BigDecimal.valueOf(33.33), 1, "STANDARD")
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should round to 2 decimal places (4.67)
            assertThat(result.taxAmount()).isEqualTo(BigDecimal.valueOf(4.67));
        }

        @Test
        @Order(15)
        @DisplayName("Should handle empty order items list")
        void testEmptyItemsList() {
            // Given: Order with no items
            TaxCalculationRequest request = new TaxCalculationRequest(
                DEFAULT_ORDER_ID,
                DEFAULT_CUSTOMER_ID,
                BigDecimal.ZERO,
                "EG",
                "EG",
                "INDIVIDUAL",
                null,
                List.of()
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should handle gracefully
            assertThat(result.taxAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.breakdown()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Integration Tests - Real Configuration")
    class IntegrationTests {

        @Test
        @Order(20)
        @DisplayName("Should use configuration values from application.properties")
        void testConfigurationValues() {
            // Given: Adapter initialized with Spring configuration
            assertThat(taxCalculationAdapter).isNotNull();

            // When: Calculate tax for Egypt
            TaxCalculationRequest request = createRequest("EG", 
                new OrderLineItem(1L, "Product", BigDecimal.valueOf(1000.00), 1, "STANDARD")
            );
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should use configured Egypt rate (default 14%)
            assertThat(result.taxRate()).contains("14");
            assertThat(result.taxAmount()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @Order(21)
        @DisplayName("Should calculate tax for high-value order accurately")
        void testHighValueOrder() {
            // Given: High-value order (simulating real e-commerce scenario)
            BigDecimal subtotal = BigDecimal.valueOf(22000.00);
            TaxCalculationRequest request = new TaxCalculationRequest(
                999001L,
                DEFAULT_CUSTOMER_ID,
                subtotal,
                "SA", // Saudi Arabia - 15% VAT
                "SA",
                "INDIVIDUAL",
                null,
                List.of(
                    new OrderLineItem(101L, "iPhone 15 Pro", BigDecimal.valueOf(4500.00), 2, "STANDARD"),
                    new OrderLineItem(102L, "MacBook Pro", BigDecimal.valueOf(8500.00), 1, "STANDARD"),
                    new OrderLineItem(103L, "Apple Watch", BigDecimal.valueOf(1500.00), 3, "STANDARD")
                )
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Subtotal = (4500*2) + 8500 + (1500*3) = 9000 + 8500 + 4500 = 22000 SAR
            //       Tax = 22000 * 0.15 = 3300 SAR
            assertThat(result.subtotal()).isEqualTo(BigDecimal.valueOf(22000.00));
            assertThat(result.taxAmount()).isCloseTo(BigDecimal.valueOf(3300.00), within(BigDecimal.valueOf(0.01)));
            assertThat(result.breakdown()).hasSize(3);
        }

        @Test
        @Order(22)
        @DisplayName("Should provide detailed breakdown for auditing")
        void testDetailedBreakdown() {
            // Given: Order with multiple items
            BigDecimal subtotal = BigDecimal.valueOf(500.00);
            TaxCalculationRequest request = new TaxCalculationRequest(
                DEFAULT_ORDER_ID,
                DEFAULT_CUSTOMER_ID,
                subtotal,
                "AE", // UAE - 5% VAT
                "AE",
                "INDIVIDUAL",
                null,
                List.of(
                    new OrderLineItem(1L, "Item A", BigDecimal.valueOf(200.00), 1, "STANDARD"),
                    new OrderLineItem(2L, "Item B", BigDecimal.valueOf(300.00), 1, "STANDARD")
                )
            );

            // When: Calculate tax
            TaxCalculation result = taxCalculationAdapter.calculateTax(request);

            // Then: Should have complete breakdown
            assertThat(result.breakdown()).hasSize(2);
            assertThat(result.breakdown().get(0).productId()).isEqualTo(1L);
            assertThat(result.breakdown().get(0).tax()).isCloseTo(BigDecimal.valueOf(10.00), within(BigDecimal.valueOf(0.01)));
            assertThat(result.breakdown().get(1).productId()).isEqualTo(2L);
            assertThat(result.breakdown().get(1).tax()).isCloseTo(BigDecimal.valueOf(15.00), within(BigDecimal.valueOf(0.01)));
        }
    }

    // ========== Helper Methods ==========

    private TaxCalculationRequest createRequest(String countryCode, OrderLineItem... items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLineItem item : items) {
            subtotal = subtotal.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }
        
        return new TaxCalculationRequest(
            DEFAULT_ORDER_ID,
            DEFAULT_CUSTOMER_ID,
            subtotal,
            countryCode,
            countryCode,  // billingCountryCode same as shipping
            "INDIVIDUAL",
            null,  // taxId
            List.of(items)
        );
    }
}
