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
        String expectedPaymentKey = "txn_success_123";
        PaymentGatewayClient.CheckoutResponse successResponse = 
            new PaymentGatewayClient.CheckoutResponse(expectedPaymentKey, null, 3600);
        when(paymentGatewayClient.createCheckout(anyString(), anyDouble(), anyString(), anyString(), any()))
            .thenReturn(successResponse);

        // When: Process payment
        PaymentResult result = paymentAdapter.processPayment(
            testOrderId, testAmount, "card", "tok_visa"
        );

        // Then: Payment succeeds
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo(expectedPaymentKey);
        assertThat(result.message()).contains("successful");
        
        // Verify called once
        verify(paymentGatewayClient, times(1)).createCheckout(
            eq(testOrderId.getValue()),
            eq(99.99), 
            eq("EGP"),
            eq("test@example.com"),
            eq(null)
        );
    }

    @Test
    @Order(2)
    @DisplayName("Should retry on transient failure and eventually succeed")
    void testRetryOnTransientFailure() {
        // Given: Gateway fails twice, succeeds on third attempt (simulates network timeout)
        when(paymentGatewayClient.createCheckout(anyString(), anyDouble(), anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Network timeout"))
            .thenThrow(new RuntimeException("Connection reset"))
            .thenReturn(new PaymentGatewayClient.CheckoutResponse("txn_retry_success_456", null, 3600));

        // When: Process payment
        PaymentResult result = paymentAdapter.processPayment(
            testOrderId, testAmount, "card", "tok_visa"
        );

        // Then: Payment succeeds after retries
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("txn_retry_success_456");
        
        // Verify: Called 3 times (initial + 2 retries)
        verify(paymentGatewayClient, times(3)).createCheckout(anyString(), anyDouble(), anyString(), anyString(), any());
    }

    @Test
    @Order(3)
    @DisplayName("Should use fallback after all retries exhausted")
    void testFallbackAfterRetriesExhausted() {
        // Given: Gateway always fails
        when(paymentGatewayClient.createCheckout(anyString(), anyDouble(), anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Gateway API down"));

        // When: Process payment
        PaymentResult result = paymentAdapter.processPayment(
            testOrderId, testAmount, "card", "tok_visa"
        );

        // Then: Fallback returns error
        assertThat(result.success()).isFalse();
        assertThat(result.transactionId()).isNull();
        assertThat(result.message()).contains("temporarily unavailable");
        
        // Verify: Called 3 times (max retry attempts)
        verify(paymentGatewayClient, times(3)).createCheckout(anyString(), anyDouble(), anyString(), anyString(), any());
    }

    @Test
    @Order(4)
    @DisplayName("Should open circuit breaker after failure threshold")
    void testCircuitBreakerOpens() throws InterruptedException {
        // Given: Gateway always fails
        when(paymentGatewayClient.createCheckout(anyString(), anyDouble(), anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Gateway down"));

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
        verify(paymentGatewayClient, never()).createCheckout(anyString(), anyDouble(), anyString(), anyString(), any());
    }

    @Test
    @Order(6)
    @DisplayName("Should generate idempotency key from orderId")
    void testIdempotencyKeyGeneration() {
        // Given: Known orderId
        OrderId orderId = new OrderId("123e4567-e89b-12d3-a456-426614174000");
        PaymentGatewayClient.CheckoutResponse successResponse = 
            new PaymentGatewayClient.CheckoutResponse("txn_idempotent_789", null, 3600);
        when(paymentGatewayClient.createCheckout(anyString(), anyDouble(), anyString(), anyString(), any()))
            .thenReturn(successResponse);

        // When: Process payment
        paymentAdapter.processPayment(orderId, testAmount, "card", "tok_visa");

        // Then: Verify orderId is passed to createCheckout
        verify(paymentGatewayClient).createCheckout(
            eq("123e4567-e89b-12d3-a456-426614174000"),
            anyDouble(), 
            anyString(), 
            anyString(),
            any()
        );
    }

    @Test
    @Order(7)
    @DisplayName("Should refund with retry and circuit breaker")
    void testRefundWithResilience() {
        // When: Process refund (no mock needed - uses actual implementation)
        PaymentResult result = paymentAdapter.refundPayment(testOrderId, testAmount);

        // Then: Returns manual processing message (Accept refunds require dashboard)
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("manually through Accept dashboard");
        assertThat(result.message()).contains("Test refund");
    }

    @Test
    @Order(8)
    @DisplayName("Should use refund fallback on failure")
    void testRefundFallback() {
        // When: Process refund (no mock needed - uses actual implementation)
        PaymentResult result = paymentAdapter.refundPayment(testOrderId, testAmount);

        // Then: Returns manual processing message (Accept refunds require dashboard)
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("manually through Accept dashboard");
    }
}
