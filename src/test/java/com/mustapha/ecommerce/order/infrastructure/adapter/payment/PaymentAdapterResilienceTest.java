package com.mustapha.ecommerce.order.infrastructure.adapter.payment;

import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentResult;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk.PaymentGatewayClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Suite: Payment Adapter Resilience Patterns
 * 
 * Tests:
 * 1. Retry Logic: Automatic retry on transient failures
 * 2. Circuit Breaker: Prevents cascade failures when payment gateway is down
 * 3. Idempotency: Same orderId produces same transaction (safe to retry)
 * 4. Fallback: Returns error message when circuit is open
 * 
 * Payment Gateways Supported:
 * - Generic implementation works with any gateway (Paymob, Fawry, PayTabs, etc.)
 * 
 * Scenarios:
 * - Transient network errors (retry succeeds)
 * - Permanent failures (circuit opens after threshold)
 * - Circuit recovery (half-open -> closed)
 * - Idempotent retry (same txnId returned)
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentAdapterResilienceTest {

    @Autowired
    private PaymentAdapter paymentAdapter;

    @MockBean
    private PaymentGatewayClient paymentGatewayClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private OrderId testOrderId;
    private Money testAmount;

    @BeforeEach
    void setUp() {
        testOrderId = OrderId.generate();
        testAmount = new Money(new BigDecimal("99.99"));
        
        // Reset circuit breaker before each test
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentService");
        circuitBreaker.reset();
        
        // Clear idempotency store
        paymentGatewayClient.clearIdempotencyStore();
    }

    @Test
    @Order(1)
    @DisplayName("Should succeed on first attempt with valid payment")
    void testSuccessfulPayment() {
        // Given: Payment gateway returns success
        String expectedTxnId = "txn_success_123";
        when(paymentGatewayClient.chargeWithIdempotency(anyDouble(), anyString(), anyString()))
            .thenReturn(expectedTxnId);

        // When: Process payment
        PaymentResult result = paymentAdapter.processPayment(
            testOrderId, testAmount, "card", "tok_visa"
        );

        // Then: Payment succeeds
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo(expectedTxnId);
        assertThat(result.message()).contains("successful");
        
        // Verify called once with idempotency key
        verify(paymentGatewayClient, times(1)).chargeWithIdempotency(
            eq(99.99), 
            eq("tok_visa"), 
            startsWith("payment_")
        );
    }

    @Test
    @Order(2)
    @DisplayName("Should retry on transient failure and eventually succeed")
    void testRetryOnTransientFailure() {
        // Given: Stripe fails twice, succeeds on third attempt (simulates network timeout)
        when(paymentGatewayClient.chargeWithIdempotency(anyDouble(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Network timeout"))
            .thenThrow(new RuntimeException("Connection reset"))
            .thenReturn("txn_retry_success_456");

        // When: Process payment
        PaymentResult result = paymentAdapter.processPayment(
            testOrderId, testAmount, "card", "tok_visa"
        );

        // Then: Payment succeeds after retries
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("txn_retry_success_456");
        
        // Verify: Called 3 times (initial + 2 retries)
        verify(paymentGatewayClient, times(3)).chargeWithIdempotency(anyDouble(), anyString(), anyString());
    }

    @Test
    @Order(3)
    @DisplayName("Should use fallback after all retries exhausted")
    void testFallbackAfterRetriesExhausted() {
        // Given: Stripe always fails
        when(paymentGatewayClient.chargeWithIdempotency(anyDouble(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Stripe API down"));

        // When: Process payment
        PaymentResult result = paymentAdapter.processPayment(
            testOrderId, testAmount, "card", "tok_visa"
        );

        // Then: Fallback returns error
        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).isNull();
        assertThat(result.message()).contains("temporarily unavailable");
        assertThat(result.message()).contains(testOrderId.getValue());
        
        // Verify: Called 3 times (max retry attempts)
        verify(paymentGatewayClient, times(3)).chargeWithIdempotency(anyDouble(), anyString(), anyString());
    }

    @Test
    @Order(4)
    @DisplayName("Should open circuit breaker after failure threshold")
    void testCircuitBreakerOpens() throws InterruptedException {
        // Given: Stripe always fails
        when(paymentGatewayClient.chargeWithIdempotency(anyDouble(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Stripe down"));

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentService");
        
        // When: Fail multiple times to trigger circuit
        // Circuit config: minimumNumberOfCalls=5, failureRateThreshold=50%
        for (int i = 0; i < 6; i++) {
            OrderId orderId = OrderId.generate();
            try {
                paymentAdapter.processPayment(orderId, testAmount, "card", "tok_" + i);
            } catch (Exception e) {
                // Expected to fail
            }
            Thread.sleep(100); // Small delay between calls
        }

        // Then: Circuit should be OPEN
        assertThat(circuitBreaker.getState()).isIn(
            CircuitBreaker.State.OPEN, 
            CircuitBreaker.State.HALF_OPEN
        );
    }

    @Test
    @Order(5)
    @DisplayName("Should use fallback when circuit is open")
    void testFallbackWhenCircuitOpen() {
        // Given: Circuit is already open (from previous test)
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentService");
        circuitBreaker.transitionToOpenState();

        // When: Try to process payment
        PaymentResult result = paymentAdapter.processPayment(
            testOrderId, testAmount, "card", "tok_visa"
        );

        // Then: Fallback is called immediately (no retry)
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("temporarily unavailable");
        
        // Verify: Stripe not called (circuit is open)
        verify(paymentGatewayClient, never()).chargeWithIdempotency(anyDouble(), anyString(), anyString());
    }

    @Test
    @Order(6)
    @DisplayName("Should generate idempotency key from orderId")
    void testIdempotencyKeyGeneration() {
        // Given: Known orderId
        OrderId orderId = new OrderId("123e4567-e89b-12d3-a456-426614174000");
        when(paymentGatewayClient.chargeWithIdempotency(anyDouble(), anyString(), anyString()))
            .thenReturn("txn_idempotent_789");

        // When: Process payment
        paymentAdapter.processPayment(orderId, testAmount, "card", "tok_visa");

        // Then: Idempotency key should be "payment_<orderId>"
        verify(paymentGatewayClient).chargeWithIdempotency(
            anyDouble(), 
            anyString(), 
            eq("payment_123e4567-e89b-12d3-a456-426614174000")
        );
    }

    @Test
    @Order(7)
    @DisplayName("Should refund with retry and circuit breaker")
    void testRefundWithResilience() {
        // Given: Refund succeeds
        when(paymentGatewayClient.refundWithIdempotency(anyString(), anyDouble(), anyString()))
            .thenReturn("refund_success_999");

        // When: Process refund
        PaymentResult result = paymentAdapter.refundPayment(testOrderId, testAmount);

        // Then: Refund succeeds
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("refund_success_999");
        assertThat(result.message()).contains("Refund processed successfully");
        
        // Verify: Called with idempotency key
        verify(paymentGatewayClient).refundWithIdempotency(
            eq(testOrderId.getValue()), 
            eq(99.99), 
            startsWith("refund_")
        );
    }

    @Test
    @Order(8)
    @DisplayName("Should use refund fallback on failure")
    void testRefundFallback() {
        // Given: Refund always fails
        when(paymentGatewayClient.refundWithIdempotency(anyString(), anyDouble(), anyString()))
            .thenThrow(new RuntimeException("Refund API down"));

        // When: Process refund
        PaymentResult result = paymentAdapter.refundPayment(testOrderId, testAmount);

        // Then: Fallback returns error
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("temporarily unavailable");
        assertThat(result.message()).contains("contact support");
    }
}
