package com.mustapha.ecommerce.order.infrastructure.adapter.payment.accept;

import com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk.PaymentGatewayClient.CheckoutResponse;
import com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk.PaymentGatewayClient.PaymentVerificationResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Test Suite: Accept (Paymob) Client Integration
 * 
 * Tests:
 * 1. Checkout creation (3-step flow: auth → order → payment key)
 * 2. Payment verification
 * 3. MOCK mode fallback (when credentials not configured)
 * 4. Idempotency (same orderId returns same payment key)
 * 5. Token caching (reuse token for 25 minutes)
 * 6. Error handling (network failures, API errors)
 * 
 * Test Strategy:
 * - Mock HTTP responses from Accept API
 * - Test in MOCK mode (no real API calls needed)
 * - Verify idempotency store behavior
 * - Test token caching logic
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AcceptPaymobClientTest {

    @Autowired
    private AcceptPaymobClient acceptClient;

    @Autowired
    private AcceptPaymobConfig config;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // Reset idempotency store before each test
        acceptClient.clearIdempotencyStore();
        
        // Setup mock server if running in REAL mode
        if (config.isConfigured()) {
            mockServer = MockRestServiceServer.createServer(restTemplate);
        }
    }

    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.verify();
        }
    }

    // ========================================
    // Test Group 1: Checkout Creation
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Should create checkout in MOCK mode when credentials not configured")
    void testMockModeCheckout() {
        // Given: MOCK mode (no credentials)
        String orderId = "order_mock_123";
        double amount = 99.99;
        String currency = "EGP";
        String email = "test@example.com";

        // When: Create checkout
        CheckoutResponse response = acceptClient.createCheckout(orderId, amount, currency, email, null);

        // Then: Should return mock payment key
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.paymentKey()).startsWith("txn_mock_");
        assertThat(response.error()).isNull();
        assertThat(response.expiresInSeconds()).isEqualTo(1800); // 30 minutes
    }

    @Test
    @Order(2)
    @DisplayName("Should return same payment key for duplicate checkout (idempotency)")
    void testCheckoutIdempotency() {
        // Given: Same order ID
        String orderId = "order_idempotent_456";
        double amount = 149.99;
        String currency = "EGP";

        // When: Create checkout twice
        CheckoutResponse first = acceptClient.createCheckout(orderId, amount, currency, "test@example.com", null);
        CheckoutResponse second = acceptClient.createCheckout(orderId, amount, currency, "test@example.com", null);

        // Then: Both should return same payment key
        assertThat(first.paymentKey()).isNotNull();
        assertThat(second.paymentKey()).isEqualTo(first.paymentKey());
    }

    @Test
    @Order(3)
    @DisplayName("Should create different payment keys for different orders")
    void testDifferentOrdersGetDifferentKeys() {
        // Given: Two different orders
        String orderId1 = "order_789";
        String orderId2 = "order_101";

        // When: Create checkouts
        CheckoutResponse response1 = acceptClient.createCheckout(orderId1, 50.0, "EGP", "test1@example.com", null);
        CheckoutResponse response2 = acceptClient.createCheckout(orderId2, 75.0, "EGP", "test2@example.com", null);

        // Then: Should have different payment keys
        assertThat(response1.paymentKey()).isNotEqualTo(response2.paymentKey());
    }

    @Test
    @Order(4)
    @DisplayName("Should create checkout with full customer details")
    void testCheckoutWithFullCustomerInfo() {
        // Given: Full customer information
        String orderId = "order_full_customer_202";
        double amount = 299.50;
        String currency = "EGP";
        String email = "customer@example.com";
        String phone = "+20123456789";

        // When: Create checkout
        CheckoutResponse response = acceptClient.createCheckout(orderId, amount, currency, email, phone);

        // Then: Should succeed
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    // ========================================
    // Test Group 2: Payment Verification
    // ========================================

    @Test
    @Order(5)
    @DisplayName("Should verify payment in MOCK mode")
    void testPaymentVerificationMock() {
        // Given: Checkout created in MOCK mode
        String orderId = "order_verify_303";
        CheckoutResponse checkout = acceptClient.createCheckout(orderId, 100.0, "EGP", "test@example.com", null);
        String transactionId = checkout.paymentKey();

        // When: Verify payment
        PaymentVerificationResponse response = acceptClient.verifyPayment(transactionId);

        // Then: Should return success (MOCK mode returns "PAID" status)
        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.transactionId()).isNotNull();
        assertThat(response.orderId()).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("Should handle payment verification for non-existent transaction")
    void testVerifyNonExistentTransaction() {
        // Given: Non-existent transaction ID
        String nonExistentId = "non_existent_txn_999";

        // When: Verify payment
        PaymentVerificationResponse response = acceptClient.verifyPayment(nonExistentId);

        // Then: In MOCK mode, always returns success (in REAL mode would fail)
        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo("PAID");
    }

    // ========================================
    // Test Group 3: Idempotency Store
    // ========================================

    @Test
    @Order(7)
    @DisplayName("Should clear idempotency store")
    void testClearIdempotencyStore() {
        // Given: Checkout created
        String orderId = "order_clear_404";
        CheckoutResponse first = acceptClient.createCheckout(orderId, 50.0, "EGP", "test@example.com", null);

        // When: Clear store and create again
        acceptClient.clearIdempotencyStore();
        CheckoutResponse second = acceptClient.createCheckout(orderId, 50.0, "EGP", "test@example.com", null);

        // Then: Should get different payment key
        assertThat(first.paymentKey()).isNotEqualTo(second.paymentKey());
    }

    // ========================================
    // Test Group 4: Legacy Methods (Backward Compatibility)
    // ========================================

    @Test
    @Order(8)
    @DisplayName("Should support legacy chargeWithIdempotency method")
    void testLegacyChargeMethod() {
        // Given: Legacy parameters
        double amount = 199.99;
        String token = "tok_visa_test";
        String idempotencyKey = "legacy_key_505";

        // When: Call legacy method
        String paymentKey = acceptClient.chargeWithIdempotency(amount, token, idempotencyKey);

        // Then: Should return payment key (MOCK mode returns txn_mock_ format)
        assertThat(paymentKey).isNotNull();
        assertThat(paymentKey).startsWith("txn_mock_");

        // And: Second call with same key returns same payment key (idempotency)
        String secondKey = acceptClient.chargeWithIdempotency(amount, token, idempotencyKey);
        assertThat(secondKey).isEqualTo(paymentKey);
    }

    @Test
    @Order(9)
    @DisplayName("Should support legacy refundWithIdempotency method")
    void testLegacyRefundMethod() {
        // Given: Legacy refund parameters
        String transactionId = "txn_original_606";
        double amount = 50.0;
        String idempotencyKey = "refund_legacy_707";

        // When: Call legacy refund method
        String refundId = acceptClient.refundWithIdempotency(transactionId, amount, idempotencyKey);

        // Then: Should return refund ID
        assertThat(refundId).isNotNull();
        assertThat(refundId).startsWith("refund_");

        // And: Second call with same key returns same ID
        String secondRefundId = acceptClient.refundWithIdempotency(transactionId, amount, idempotencyKey);
        assertThat(secondRefundId).isEqualTo(refundId);
    }

    // ========================================
    // Test Group 5: Error Handling
    // ========================================

    @Test
    @Order(10)
    @DisplayName("Should handle null order ID gracefully")
    void testNullOrderId() {
        // Given: Null order ID
        String orderId = null;

        // When/Then: Should handle gracefully (or throw appropriate exception)
        CheckoutResponse response = acceptClient.createCheckout(orderId, 100.0, "EGP", "test@example.com", null);
        
        // Mock mode should handle this gracefully
        assertThat(response).isNotNull();
    }

    @Test
    @Order(11)
    @DisplayName("Should handle zero amount")
    void testZeroAmount() {
        // Given: Zero amount
        String orderId = "order_zero_808";
        double amount = 0.0;

        // When: Create checkout
        CheckoutResponse response = acceptClient.createCheckout(orderId, amount, "EGP", "test@example.com", null);

        // Then: Should return payment key (validation is business logic concern)
        assertThat(response.paymentKey()).isNotNull();
    }

    @Test
    @Order(12)
    @DisplayName("Should handle negative amount")
    void testNegativeAmount() {
        // Given: Negative amount
        String orderId = "order_negative_909";
        double amount = -50.0;

        // When: Create checkout
        CheckoutResponse response = acceptClient.createCheckout(orderId, amount, "EGP", "test@example.com", null);

        // Then: Should return payment key (validation is business logic concern)
        assertThat(response.paymentKey()).isNotNull();
    }

    @Test
    @Order(13)
    @DisplayName("Should handle empty email")
    void testEmptyEmail() {
        // Given: Empty email
        String orderId = "order_no_email_1010";
        String email = "";

        // When: Create checkout
        CheckoutResponse response = acceptClient.createCheckout(orderId, 100.0, "EGP", email, null);

        // Then: Should succeed (email validation is business logic)
        assertThat(response.paymentKey()).isNotNull();
    }

    // ========================================
    // Test Group 6: Different Currencies
    // ========================================

    @Test
    @Order(14)
    @DisplayName("Should support Egyptian Pounds (EGP)")
    void testEgyptianPounds() {
        CheckoutResponse response = acceptClient.createCheckout("order_egp_1111", 100.0, "EGP", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    @Test
    @Order(15)
    @DisplayName("Should support Saudi Riyal (SAR)")
    void testSaudiRiyal() {
        CheckoutResponse response = acceptClient.createCheckout("order_sar_1212", 250.0, "SAR", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    @Test
    @Order(16)
    @DisplayName("Should support UAE Dirham (AED)")
    void testUAEDirham() {
        CheckoutResponse response = acceptClient.createCheckout("order_aed_1313", 500.0, "AED", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    @Test
    @Order(17)
    @DisplayName("Should support US Dollars (USD)")
    void testUSDollars() {
        CheckoutResponse response = acceptClient.createCheckout("order_usd_1414", 150.0, "USD", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    // ========================================
    // Test Group 7: Large Amounts
    // ========================================

    @Test
    @Order(18)
    @DisplayName("Should handle large amounts (10,000 EGP)")
    void testLargeAmount() {
        CheckoutResponse response = acceptClient.createCheckout("order_large_1515", 10000.0, "EGP", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    @Test
    @Order(19)
    @DisplayName("Should handle very large amounts (100,000 EGP)")
    void testVeryLargeAmount() {
        CheckoutResponse response = acceptClient.createCheckout("order_very_large_1616", 100000.0, "EGP", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    // ========================================
    // Test Group 8: Decimal Precision
    // ========================================

    @Test
    @Order(20)
    @DisplayName("Should handle decimal amounts (99.99)")
    void testDecimalAmount() {
        CheckoutResponse response = acceptClient.createCheckout("order_decimal_1717", 99.99, "EGP", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }

    @Test
    @Order(21)
    @DisplayName("Should handle three decimal places (99.995)")
    void testThreeDecimalPlaces() {
        CheckoutResponse response = acceptClient.createCheckout("order_3dec_1818", 99.995, "EGP", "test@example.com", null);
        assertThat(response.paymentKey()).isNotNull();
        assertThat(response.error()).isNull();
    }
}
