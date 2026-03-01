package com.mustapha.ecommerce.order.api.rest;

import com.mustapha.ecommerce.config.WebMvcTestConfig;
import com.mustapha.ecommerce.order.application.command.InitiatePaymentCommand;
import com.mustapha.ecommerce.order.application.command.VerifyPaymentCommand;
import com.mustapha.ecommerce.order.application.port.PaymentPort.CheckoutResult;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentStatus;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentVerificationResult;
import com.mustapha.ecommerce.order.application.usecase.InitiatePaymentUseCase;
import com.mustapha.ecommerce.order.application.usecase.VerifyPaymentUseCase;
import com.mustapha.ecommerce.shared.security.ExponentialBackoffFilter;
import com.mustapha.ecommerce.shared.security.GlobalApiRateLimitFilter;
import com.mustapha.ecommerce.shared.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test Suite: Payment REST Controller
 * 
 * Tests:
 * 1. POST /api/payments/checkout - Initiate payment
 * 2. POST /api/payments/verify - Verify payment
 * 3. GET /api/payments/health - Health check
 * 4. Request validation
 * 5. Error handling
 * 
 * Test Strategy:
 * - Use @WebMvcTest for controller layer testing
 * - Mock use case dependencies
 * - Test HTTP request/response handling
 * - Verify JSON serialization
 */
@WebMvcTest(value = PaymentController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {JwtAuthenticationFilter.class, ExponentialBackoffFilter.class, GlobalApiRateLimitFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InitiatePaymentUseCase initiatePaymentUseCase;

    @MockBean
    private VerifyPaymentUseCase verifyPaymentUseCase;

    // ========================================
    // Test Group 1: Initiate Payment (Checkout)
    // ========================================

    @Test
    @Order(1)
    @DisplayName("POST /api/payments/checkout - Should create checkout successfully")
    void testInitiatePaymentSuccess() throws Exception {
        // Given: Valid checkout request
        CheckoutResult mockResult = new CheckoutResult(
            true,
            "payment_key_abc123",
            "https://accept.paymob.com/iframe/123?payment_token=payment_key_abc123",
            1800,
            "Checkout session created successfully"
        );
        
        when(initiatePaymentUseCase.execute(any(InitiatePaymentCommand.class)))
            .thenReturn(mockResult);

        // When/Then: POST /api/payments/checkout
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "order_123",
                        "paymentMethod": "VISA",
                        "customerEmail": "customer@example.com",
                        "shopperResultUrl": "https://example.com/payment/return"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.checkoutId").value("payment_key_abc123"))
                .andExpect(jsonPath("$.redirectUrl").value(containsString("accept.paymob.com")))
                .andExpect(jsonPath("$.expiresInSeconds").value(1800))
                .andExpect(jsonPath("$.message").exists());

        verify(initiatePaymentUseCase, times(1)).execute(any(InitiatePaymentCommand.class));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/payments/checkout - Should reject request without orderId")
    void testInitiatePaymentMissingOrderId() throws Exception {
        // When/Then: POST without orderId
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "paymentMethod": "VISA",
                        "customerEmail": "customer@example.com"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("orderId")));

        verify(initiatePaymentUseCase, never()).execute(any());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/payments/checkout - Should reject request without paymentMethod")
    void testInitiatePaymentMissingPaymentMethod() throws Exception {
        // When/Then: POST without paymentMethod
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "order_123",
                        "customerEmail": "customer@example.com"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("paymentMethod")));

        verify(initiatePaymentUseCase, never()).execute(any());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/payments/checkout - Should reject request without customerEmail")
    void testInitiatePaymentMissingEmail() throws Exception {
        // When/Then: POST without customerEmail
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "order_123",
                        "paymentMethod": "VISA"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("customerEmail")));

        verify(initiatePaymentUseCase, never()).execute(any());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/payments/checkout - Should reject request with blank orderId")
    void testInitiatePaymentBlankOrderId() throws Exception {
        // When/Then: POST with blank orderId
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "  ",
                        "paymentMethod": "VISA",
                        "customerEmail": "customer@example.com"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("orderId")));

        verify(initiatePaymentUseCase, never()).execute(any());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/payments/checkout - Should handle payment gateway failure")
    void testInitiatePaymentGatewayFailure() throws Exception {
        // Given: Payment gateway failure
        CheckoutResult mockResult = new CheckoutResult(
            false,
            null,
            null,
            0,
            "Payment gateway unavailable"
        );
        
        when(initiatePaymentUseCase.execute(any(InitiatePaymentCommand.class)))
            .thenReturn(mockResult);

        // When/Then: POST /api/payments/checkout
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "order_failed",
                        "paymentMethod": "VISA",
                        "customerEmail": "customer@example.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.checkoutId").value(""))
                .andExpect(jsonPath("$.redirectUrl").value(""))
                .andExpect(jsonPath("$.message").value("Payment gateway unavailable"));
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/payments/checkout - Should support different payment methods")
    void testInitiatePaymentDifferentMethods() throws Exception {
        // Given: Mock checkout
        CheckoutResult mockResult = new CheckoutResult(true, "key", "url", 1800, "Success");
        when(initiatePaymentUseCase.execute(any())).thenReturn(mockResult);

        // Test MASTERCARD
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "order_mc",
                        "paymentMethod": "MASTERCARD",
                        "customerEmail": "test@example.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test MADA
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "order_mada",
                        "paymentMethod": "MADA",
                        "customerEmail": "test@example.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(initiatePaymentUseCase, times(2)).execute(any());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/payments/checkout - Should handle IllegalArgumentException")
    void testInitiatePaymentIllegalArgument() throws Exception {
        // Given: Use case throws IllegalArgumentException
        when(initiatePaymentUseCase.execute(any(InitiatePaymentCommand.class)))
            .thenThrow(new IllegalArgumentException("Invalid order ID format"));

        // When/Then: Should return 400 Bad Request
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "invalid",
                        "paymentMethod": "VISA",
                        "customerEmail": "test@example.com"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid order ID format"));
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/payments/checkout - Should handle IllegalStateException")
    void testInitiatePaymentIllegalState() throws Exception {
        // Given: Use case throws IllegalStateException
        when(initiatePaymentUseCase.execute(any(InitiatePaymentCommand.class)))
            .thenThrow(new IllegalStateException("Order already paid"));

        // When/Then: Should return 200 OK with success=false
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderId": "order_paid",
                        "paymentMethod": "VISA",
                        "customerEmail": "test@example.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Order already paid"));
    }

    // ========================================
    // Test Group 2: Verify Payment
    // ========================================

    @Test
    @Order(10)
    @DisplayName("POST /api/payments/verify - Should verify successful payment")
    void testVerifyPaymentSuccess() throws Exception {
        // Given: Successful payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_abc123",
            PaymentStatus.SUCCESS,
            "Payment verified successfully"
        );
        
        when(verifyPaymentUseCase.execute(any(VerifyPaymentCommand.class)))
            .thenReturn(mockResult);

        // When/Then: POST /api/payments/verify
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "checkoutId": "payment_key_abc123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("txn_abc123"))
                .andExpect(jsonPath("$.message").value("Payment verified successfully"));

        verify(verifyPaymentUseCase, times(1)).execute(any(VerifyPaymentCommand.class));
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/payments/verify - Should handle failed payment")
    void testVerifyPaymentFailed() throws Exception {
        // Given: Failed payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            "txn_failed",
            PaymentStatus.FAILED,
            "Card declined by issuer"
        );
        
        when(verifyPaymentUseCase.execute(any(VerifyPaymentCommand.class)))
            .thenReturn(mockResult);

        // When/Then: POST /api/payments/verify
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "checkoutId": "payment_key_failed"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Card declined by issuer"));
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/payments/verify - Should handle pending payment")
    void testVerifyPaymentPending() throws Exception {
        // Given: Pending payment
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            "txn_pending",
            PaymentStatus.PENDING,
            "Payment is being processed"
        );
        
        when(verifyPaymentUseCase.execute(any(VerifyPaymentCommand.class)))
            .thenReturn(mockResult);

        // When/Then: POST /api/payments/verify
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "checkoutId": "payment_key_pending"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Payment is being processed"));
    }

    @Test
    @Order(13)
    @DisplayName("POST /api/payments/verify - Should reject request without checkoutId")
    void testVerifyPaymentMissingCheckoutId() throws Exception {
        // When/Then: POST without checkoutId
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("checkoutId")));

        verify(verifyPaymentUseCase, never()).execute(any());
    }

    @Test
    @Order(14)
    @DisplayName("POST /api/payments/verify - Should reject request with blank checkoutId")
    void testVerifyPaymentBlankCheckoutId() throws Exception {
        // When/Then: POST with blank checkoutId
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "checkoutId": "   "
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("checkoutId")));

        verify(verifyPaymentUseCase, never()).execute(any());
    }

    @Test
    @Order(15)
    @DisplayName("POST /api/payments/verify - Should handle IllegalStateException")
    void testVerifyPaymentIllegalState() throws Exception {
        // Given: Use case throws IllegalStateException
        when(verifyPaymentUseCase.execute(any(VerifyPaymentCommand.class)))
            .thenThrow(new IllegalStateException("Payment verification timeout"));

        // When/Then: Should return 200 OK with success=false
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "checkoutId": "payment_key_timeout"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Payment verification timeout"));
    }

    @Test
    @Order(16)
    @DisplayName("POST /api/payments/verify - Should handle unexpected exception")
    void testVerifyPaymentUnexpectedException() throws Exception {
        // Given: Use case throws unexpected exception
        when(verifyPaymentUseCase.execute(any(VerifyPaymentCommand.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When/Then: Should return 500 Internal Server Error
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "checkoutId": "payment_key_error"
                    }
                    """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("Internal server error")));
    }

    // ========================================
    // Test Group 3: Health Check
    // ========================================

    @Test
    @Order(17)
    @DisplayName("GET /api/payments/health - Should return healthy status")
    void testHealthCheck() throws Exception {
        // When/Then: GET /api/payments/health
        mockMvc.perform(get("/api/payments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("payment-api"))
                .andExpect(jsonPath("$.provider").value("accept-paymob"));
    }

    // ========================================
    // Test Group 4: Content Type Validation
    // ========================================

    @Test
    @Order(18)
    @DisplayName("POST /api/payments/checkout - Should reject non-JSON content type")
    void testInitiatePaymentInvalidContentType() throws Exception {
        // When/Then: POST with text/plain should fail
        // In WebMvcTest without full context, returns 500 (can't deserialize)
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.TEXT_PLAIN)
                .content("orderId=123"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(19)
    @DisplayName("POST /api/payments/verify - Should reject non-JSON content type")
    void testVerifyPaymentInvalidContentType() throws Exception {
        // When/Then: POST with text/plain should fail
        // In WebMvcTest without full context, returns 500 (can't deserialize)
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.TEXT_PLAIN)
                .content("checkoutId=abc"))
                .andExpect(status().is5xxServerError());
    }

    // ========================================
    // Test Group 5: Malformed JSON
    // ========================================

    @Test
    @Order(20)
    @DisplayName("POST /api/payments/checkout - Should reject malformed JSON")
    void testInitiatePaymentMalformedJson() throws Exception {
        // When/Then: POST with malformed JSON
        mockMvc.perform(post("/api/payments/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{orderId: invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(21)
    @DisplayName("POST /api/payments/verify - Should reject malformed JSON")
    void testVerifyPaymentMalformedJson() throws Exception {
        // When/Then: POST with malformed JSON
        mockMvc.perform(post("/api/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{checkoutId: 'missing quotes'}"))
                .andExpect(status().isBadRequest());
    }
}
