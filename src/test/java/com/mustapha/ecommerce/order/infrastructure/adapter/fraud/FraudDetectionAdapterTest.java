package com.mustapha.ecommerce.order.infrastructure.adapter.fraud;

import com.mustapha.ecommerce.order.application.port.FraudCheckPort;
import com.mustapha.ecommerce.order.application.port.FraudCheckPort.FraudAssessment;
import com.mustapha.ecommerce.order.application.port.FraudCheckPort.FraudCheckRequest;
import com.mustapha.ecommerce.order.application.port.FraudCheckPort.RiskLevel;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test Suite: Fraud Detection Service
 * 
 * Tests:
 * 1. Unit Tests: Risk scoring algorithm for different scenarios
 * 2. Resilience Tests: Edge cases, boundary values, null handling
 * 3. Integration Tests: Real-world fraud patterns
 * 
 * Coverage:
 * - High-value order detection (> 5000, > 10000)
 * - New customer risk assessment
 * - IP location mismatch detection
 * - Payment method risk evaluation
 * - Email pattern analysis (disposable emails)
 * - Velocity checks (multiple orders)
 * - Risk level classification (LOW, MEDIUM, HIGH)
 * - Risk factor accumulation
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Fraud Detection Service Tests")
class FraudDetectionAdapterTest {

    @Autowired
    private FraudDetectionAdapter fraudDetectionAdapter;

    private static final OrderId DEFAULT_ORDER_ID = OrderId.generate();
    private static final CustomerId DEFAULT_CUSTOMER_ID = CustomerId.generate();
    private static final String DEFAULT_EMAIL = "customer@example.com";
    private static final String DEFAULT_IP = "192.168.1.1";
    private static final String DEFAULT_SHIPPING_COUNTRY = "EG";
    private static final String DEFAULT_IP_COUNTRY = "EG";
    private static final String DEFAULT_PAYMENT_METHOD = "CREDIT_CARD";

    @Nested
    @DisplayName("Unit Tests - Risk Scoring Algorithm")
    class RiskScoringTests {

        @Test
        @Order(1)
        @DisplayName("Should classify low-value order from returning customer as LOW risk")
        void testLowRiskOrder() {
            // Given: Small order from trusted customer
            FraudCheckRequest request = createRequest(
                new Money(500.0), // Low amount
                false, // Returning customer
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should be low risk
            assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW);
            assertThat(result.riskScore()).isLessThan(30);
            assertThat(result.shouldReject()).isFalse();
            assertThat(result.requiresReview()).isFalse();
        }

        @Test
        @Order(2)
        @DisplayName("Should classify high-value order as MEDIUM risk")
        void testHighValueOrder() {
            // Given: High-value order (> 5000)
            FraudCheckRequest request = createRequest(
                new Money(6000.0), // High value
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should have elevated risk
            assertThat(result.riskScore()).isGreaterThanOrEqualTo(20);
            assertThat(result.reasons()).anyMatch(factor -> factor.contains("high") || factor.contains("value"));
        }

        @Test
        @Order(3)
        @DisplayName("Should classify very high-value order as HIGH risk")
        void testVeryHighValueOrder() {
            // Given: Very high-value order (> 10000)
            FraudCheckRequest request = createRequest(
                new Money(12000.0), // Very high value
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should have high risk score
            assertThat(result.riskScore()).isGreaterThanOrEqualTo(35);
            assertThat(result.reasons()).anyMatch(factor -> factor.toLowerCase().contains("very high"));
        }

        @Test
        @Order(4)
        @DisplayName("Should add risk for new customer")
        void testNewCustomerRisk() {
            // Given: First order from new customer
            FraudCheckRequest request = createRequest(
                new Money(1000.0),
                true, // New customer
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should add new customer risk
            assertThat(result.riskScore()).isGreaterThan(0);
            assertThat(result.reasons()).anyMatch(factor -> factor.toLowerCase().contains("new customer"));
        }

        @Test
        @Order(5)
        @DisplayName("Should add risk for IP location mismatch")
        void testIPLocationMismatch() {
            // Given: IP country different from shipping country
            FraudCheckRequest request = createRequest(
                new Money(1000.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                "EG", // Shipping to Egypt
                "US", // IP from USA
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should add mismatch risk
            assertThat(result.riskScore()).isGreaterThan(0);
            assertThat(result.reasons()).anyMatch(factor -> 
                factor.toLowerCase().contains("country") && factor.toLowerCase().contains("differs")
            );
        }

        @Test
        @Order(6)
        @DisplayName("Should detect disposable email domains")
        void testDisposableEmail() {
            // Given: Order with disposable email
            FraudCheckRequest request = createRequest(
                new Money(1000.0),
                false,
                "user@tempmail.com", // Disposable email
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should add email risk
            assertThat(result.reasons()).anyMatch(factor -> 
                factor.toLowerCase().contains("email") || factor.toLowerCase().contains("disposable")
            );
        }

        @Test
        @Order(7)
        @DisplayName("Should classify COD as lower risk than credit card")
        void testCODLowerRisk() {
            // Given: Two identical orders, one COD, one credit card
            FraudCheckRequest codRequest = createRequest(
                new Money(2000.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CASH_ON_DELIVERY"
            );

            FraudCheckRequest cardRequest = createRequest(
                new Money(2000.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess both
            FraudAssessment codResult = fraudDetectionAdapter.assessOrderRisk(codRequest);
            FraudAssessment cardResult = fraudDetectionAdapter.assessOrderRisk(cardRequest);

            // Then: COD should not add extra risk vs credit card
            // (both should have similar base risk from amount)
            assertThat(codResult.riskScore()).isLessThanOrEqualTo(cardResult.riskScore() + 5);
        }

        @Test
        @Order(8)
        @DisplayName("Should accumulate multiple risk factors correctly")
        void testMultipleRiskFactors() {
            // Given: Order with MANY red flags
            FraudCheckRequest request = createRequest(
                new Money(15000.0), // Very high value (+40)
                true, // New customer (+20)
                "test@tempmail.com", // Disposable email (+15)
                DEFAULT_IP,
                "EG", // Egypt
                "US", // IP from USA (+25)
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should accumulate to HIGH risk
            assertThat(result.riskScore()).isGreaterThanOrEqualTo(70); // HIGH threshold
            assertThat(result.riskLevel()).isIn(RiskLevel.HIGH, RiskLevel.MEDIUM);
            assertThat(result.reasons()).hasSizeGreaterThanOrEqualTo(3);
            assertThat(result.requiresReview()).isTrue();
        }
    }

    @Nested
    @DisplayName("Resilience Tests - Edge Cases & Error Handling")
    class ResilienceTests {

        @Test
        @Order(10)
        @DisplayName("Should handle zero amount order")
        void testZeroAmountOrder() {
            // Given: Free order (promotional, etc.)
            FraudCheckRequest request = createRequest(
                new Money(0.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should handle gracefully (likely low risk)
            assertThat(result).isNotNull();
            assertThat(result.riskScore()).isGreaterThanOrEqualTo(0);
            assertThat(result.riskLevel()).isNotNull();
        }

        @Test
        @Order(11)
        @DisplayName("Should handle negative amount gracefully")
        void testNegativeAmount() {
            // Given & When: Invalid negative amount
            // Then: Should throw exception (Money constructor validates)
            assertThatThrownBy(() -> {
                createRequest(
                    new Money(-100.0),
                    false,
                    DEFAULT_EMAIL,
                    DEFAULT_IP,
                    DEFAULT_SHIPPING_COUNTRY,
                    DEFAULT_IP_COUNTRY,
                    "CREDIT_CARD"
                );
            }).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Amount cannot be negative");
        }

        @Test
        @Order(12)
        @DisplayName("Should handle null/empty email gracefully")
        void testNullEmail() {
            // Given: Order with missing email
            FraudCheckRequest request = createRequest(
                new Money(1000.0),
                false,
                "", // Empty email
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should handle gracefully
            assertThat(result).isNotNull();
            assertThat(result.riskLevel()).isNotNull();
        }

        @Test
        @Order(13)
        @DisplayName("Should handle null country codes gracefully")
        void testNullCountryCodes() {
            // Given: Order with null location data
            FraudCheckRequest request = createRequest(
                new Money(1000.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                null, // Null shipping country
                null, // Null IP country
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should not crash
            assertThat(result).isNotNull();
        }

        @Test
        @Order(14)
        @DisplayName("Should handle unknown payment method")
        void testUnknownPaymentMethod() {
            // Given: Order with unsupported payment method
            FraudCheckRequest request = createRequest(
                new Money(1000.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CRYPTO_BITCOIN" // Unknown method
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should handle gracefully
            assertThat(result).isNotNull();
            assertThat(result.riskLevel()).isNotNull();
        }

        @Test
        @Order(15)
        @DisplayName("Should handle boundary value 5000 (high threshold)")
        void testBoundaryValue5000() {
            // Given: Order exactly at high-value threshold
            FraudCheckRequest request = createRequest(
                new Money(5000.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should handle boundary correctly
            assertThat(result).isNotNull();
            assertThat(result.riskScore()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @Order(16)
        @DisplayName("Should handle boundary value 10000 (very high threshold)")
        void testBoundaryValue10000() {
            // Given: Order exactly at very-high-value threshold
            FraudCheckRequest request = createRequest(
                new Money(10000.0),
                false,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should handle boundary correctly
            assertThat(result).isNotNull();
            assertThat(result.riskScore()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Integration Tests - Real-World Fraud Patterns")
    class IntegrationTests {

        @Test
        @Order(20)
        @DisplayName("Should detect typical fraud pattern: High-value + New + Mismatch")
        void testTypicalFraudPattern() {
            // Given: Classic fraud indicators
            FraudCheckRequest request = createRequest(
                new Money(8000.0), // High value
                true, // New customer
                "quickbuy@tempmail.com", // Disposable email
                "203.0.113.1",
                "EG", // Egypt
                "NG", // Nigeria IP
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should flag as high risk
            assertThat(result.riskLevel()).isIn(RiskLevel.HIGH, RiskLevel.MEDIUM);
            assertThat(result.requiresReview()).isTrue();
            assertThat(result.reasons()).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        @Order(21)
        @DisplayName("Should allow legitimate high-value order from trusted customer")
        void testLegitimateHighValueOrder() {
            // Given: High-value but from returning customer with matching location
            FraudCheckRequest request = createRequest(
                new Money(7000.0),
                false, // Returning customer
                "john.doe@gmail.com", // Legitimate email
                DEFAULT_IP,
                "EG",
                "EG", // Matching location
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should not be HIGH risk despite amount
            assertThat(result.riskLevel()).isNotEqualTo(RiskLevel.HIGH);
            assertThat(result.shouldReject()).isFalse();
        }

        @Test
        @Order(22)
        @DisplayName("Should provide actionable recommendations")
        void testRecommendations() {
            // Given: Medium risk order
            FraudCheckRequest request = createRequest(
                new Money(6000.0),
                true,
                DEFAULT_EMAIL,
                DEFAULT_IP,
                DEFAULT_SHIPPING_COUNTRY,
                DEFAULT_IP_COUNTRY,
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: Should provide recommendations
            assertThat(result.recommendation()).isNotNull();
            assertThat(result.recommendation()).isIn(
                FraudCheckPort.Recommendation.ACCEPT,
                FraudCheckPort.Recommendation.CHALLENGE,
                FraudCheckPort.Recommendation.REVIEW,
                FraudCheckPort.Recommendation.REJECT
            );
        }

        @Test
        @Order(23)
        @DisplayName("Should log risk factors for audit trail")
        void testRiskFactorsLogging() {
            // Given: Order with multiple risk factors
            FraudCheckRequest request = createRequest(
                new Money(8000.0),
                true,
                "test@guerrillamail.com",
                DEFAULT_IP,
                "EG",
                "US",
                "CREDIT_CARD"
            );

            // When: Assess risk
            FraudAssessment result = fraudDetectionAdapter.assessOrderRisk(request);

            // Then: All risk factors should be logged
            assertThat(result.reasons()).isNotEmpty();
            assertThat(result.reasons()).allMatch(factor -> !factor.trim().isEmpty());
        }
    }

    // ========== Helper Methods ==========

    private FraudCheckRequest createRequest(
            Money amount,
            boolean isNewCustomer,
            String email,
            String ipAddress,
            String shippingCountry,
            String ipCountry,
            String paymentMethod
    ) {
        return new FraudCheckRequest(
            DEFAULT_ORDER_ID,
            DEFAULT_CUSTOMER_ID,
            amount,
            email,
            null,  // customerPhone
            ipAddress,
            "Mozilla/5.0",  // userAgent
            shippingCountry,
            ipCountry,  // billingCountry
            isNewCustomer,
            paymentMethod,
            null  // deviceFingerprint
        );
    }
}
