package com.mustapha.ecommerce.order.api.webhook;

import com.mustapha.ecommerce.config.WebMvcTestConfig;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentStatus;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentVerificationResult;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test Suite: Payment Webhook Controller
 * 
 * Tests:
 * 1. GET /api/webhooks/payment/callback - Accept payment redirect
 * 2. GET /api/webhooks/payment/verify - Payment verification endpoint
 * 3. GET /api/webhooks/payment/health - Health check
 * 4. Query parameter validation
 * 5. Different payment statuses (SUCCESS, FAILED, PENDING, CANCELLED)
 * 6. Error handling
 * 
 * Test Strategy:
 * - Use @WebMvcTest for controller layer testing
 * - Mock PaymentPort dependency
 * - Test redirect behavior for different payment statuses
 * - Verify query parameter handling
 */
@WebMvcTest(value = PaymentWebhookController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {JwtAuthenticationFilter.class, ExponentialBackoffFilter.class, GlobalApiRateLimitFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentPort paymentPort;

    // ========================================
    // Test Group 1: Payment Callback (Redirect)
    // ========================================

    @Test
    @Order(1)
    @DisplayName("GET /api/webhooks/payment/callback - Should redirect to success page when payment succeeds")
    void testCallbackSuccessfulPayment() throws Exception {
        // Given: Successful payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_success_123",
            PaymentStatus.SUCCESS,
            "Payment verified successfully"
        );
        
        when(paymentPort.verifyPayment("payment_key_abc123"))
            .thenReturn(mockResult);

        // When/Then: GET /api/webhooks/payment/callback?id=payment_key_abc123
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "payment_key_abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment/success*"))
                .andExpect(redirectedUrl("/payment/success?transactionId=txn_success_123"));

        verify(paymentPort, times(1)).verifyPayment("payment_key_abc123");
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/webhooks/payment/callback - Should redirect to failure page when payment fails")
    void testCallbackFailedPayment() throws Exception {
        // Given: Failed payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            "txn_failed_456",
            PaymentStatus.FAILED,
            "Card declined by issuer"
        );
        
        when(paymentPort.verifyPayment("payment_key_failed"))
            .thenReturn(mockResult);

        // When/Then: GET /api/webhooks/payment/callback?id=payment_key_failed
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "payment_key_failed"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment/failure*"))
                .andExpect(redirectedUrl("/payment/failure?reason=Card declined by issuer"));

        verify(paymentPort, times(1)).verifyPayment("payment_key_failed");
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/webhooks/payment/callback - Should redirect to pending page when payment is pending")
    void testCallbackPendingPayment() throws Exception {
        // Given: Pending payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            "txn_pending_789",
            PaymentStatus.PENDING,
            "Payment is being processed"
        );
        
        when(paymentPort.verifyPayment("payment_key_pending"))
            .thenReturn(mockResult);

        // When/Then: GET /api/webhooks/payment/callback?id=payment_key_pending
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "payment_key_pending"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment/pending*"))
                .andExpect(redirectedUrl("/payment/pending?transactionId=txn_pending_789"));

        verify(paymentPort, times(1)).verifyPayment("payment_key_pending");
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/webhooks/payment/callback - Should redirect to cancelled page when payment is cancelled")
    void testCallbackCancelledPayment() throws Exception {
        // Given: Cancelled payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            null,
            PaymentStatus.CANCELLED,
            "Customer cancelled payment"
        );
        
        when(paymentPort.verifyPayment("payment_key_cancelled"))
            .thenReturn(mockResult);

        // When/Then: GET /api/webhooks/payment/callback?id=payment_key_cancelled
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "payment_key_cancelled"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/cancelled"));

        verify(paymentPort, times(1)).verifyPayment("payment_key_cancelled");
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/webhooks/payment/callback - Should redirect to failure when checkoutId is missing")
    void testCallbackMissingCheckoutId() throws Exception {
        // When/Then: GET /api/webhooks/payment/callback without id parameter
        mockMvc.perform(get("/api/webhooks/payment/callback"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failure?error=missing_checkout_id"));

        verify(paymentPort, never()).verifyPayment(any());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/webhooks/payment/callback - Should redirect to failure when checkoutId is blank")
    void testCallbackBlankCheckoutId() throws Exception {
        // When/Then: GET /api/webhooks/payment/callback?id=
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failure?error=missing_checkout_id"));

        verify(paymentPort, never()).verifyPayment(any());
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/webhooks/payment/callback - Should handle exception during verification")
    void testCallbackVerificationException() throws Exception {
        // Given: PaymentPort throws exception
        when(paymentPort.verifyPayment("payment_key_error"))
            .thenThrow(new RuntimeException("Payment gateway timeout"));

        // When/Then: Should redirect to failure page
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "payment_key_error"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/failure?error=internal_error"));

        verify(paymentPort, times(1)).verifyPayment("payment_key_error");
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/webhooks/payment/callback - Should support resourcePath parameter")
    void testCallbackWithResourcePath() throws Exception {
        // Given: Successful payment with resourcePath parameter
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_123",
            PaymentStatus.SUCCESS,
            "Success"
        );
        
        when(paymentPort.verifyPayment("key_123")).thenReturn(mockResult);

        // When/Then: GET with both id and resourcePath
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "key_123")
                .param("resourcePath", "/v1/checkouts/key_123/payment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment/success*"));

        verify(paymentPort, times(1)).verifyPayment("key_123");
    }

    // ========================================
    // Test Group 2: Payment Verification API
    // ========================================

    @Test
    @Order(9)
    @DisplayName("GET /api/webhooks/payment/verify - Should verify successful payment")
    void testVerifySuccessfulPayment() throws Exception {
        // Given: Successful payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_verify_123",
            PaymentStatus.SUCCESS,
            "Payment verified"
        );
        
        when(paymentPort.verifyPayment("checkout_123"))
            .thenReturn(mockResult);

        // When/Then: GET /api/webhooks/payment/verify?checkoutId=checkout_123
        mockMvc.perform(get("/api/webhooks/payment/verify")
                .param("checkoutId", "checkout_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("txn_verify_123"))
                .andExpect(jsonPath("$.message").value("Payment verified"));

        verify(paymentPort, times(1)).verifyPayment("checkout_123");
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/webhooks/payment/verify - Should verify failed payment")
    void testVerifyFailedPayment() throws Exception {
        // Given: Failed payment verification
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            "txn_failed",
            PaymentStatus.FAILED,
            "Insufficient funds"
        );
        
        when(paymentPort.verifyPayment("checkout_failed"))
            .thenReturn(mockResult);

        // When/Then: GET /api/webhooks/payment/verify?checkoutId=checkout_failed
        mockMvc.perform(get("/api/webhooks/payment/verify")
                .param("checkoutId", "checkout_failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Insufficient funds"));

        verify(paymentPort, times(1)).verifyPayment("checkout_failed");
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/webhooks/payment/verify - Should handle pending payment")
    void testVerifyPendingPayment() throws Exception {
        // Given: Pending payment
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            "txn_pending",
            PaymentStatus.PENDING,
            "Processing"
        );
        
        when(paymentPort.verifyPayment("checkout_pending"))
            .thenReturn(mockResult);

        // When/Then: GET /api/webhooks/payment/verify?checkoutId=checkout_pending
        mockMvc.perform(get("/api/webhooks/payment/verify")
                .param("checkoutId", "checkout_pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(paymentPort, times(1)).verifyPayment("checkout_pending");
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/webhooks/payment/verify - Should handle verification exception")
    void testVerifyPaymentException() throws Exception {
        // Given: PaymentPort throws exception
        when(paymentPort.verifyPayment("checkout_error"))
            .thenThrow(new RuntimeException("Network timeout"));

        // When/Then: Should return error response
        mockMvc.perform(get("/api/webhooks/payment/verify")
                .param("checkoutId", "checkout_error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Network timeout"));

        verify(paymentPort, times(1)).verifyPayment("checkout_error");
    }

    @Test
    @Order(13)
    @DisplayName("GET /api/webhooks/payment/verify - Should handle null transaction ID")
    void testVerifyPaymentNullTransactionId() throws Exception {
        // Given: Verification with null transaction ID
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            false,
            null,
            PaymentStatus.FAILED,
            "Transaction not found"
        );
        
        when(paymentPort.verifyPayment("checkout_null"))
            .thenReturn(mockResult);

        // When/Then: Should return empty string for transaction ID
        mockMvc.perform(get("/api/webhooks/payment/verify")
                .param("checkoutId", "checkout_null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(""));
    }

    @Test
    @Order(14)
    @DisplayName("GET /api/webhooks/payment/verify - Should handle null message")
    void testVerifyPaymentNullMessage() throws Exception {
        // Given: Verification with null message
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_123",
            PaymentStatus.SUCCESS,
            null
        );
        
        when(paymentPort.verifyPayment("checkout_no_msg"))
            .thenReturn(mockResult);

        // When/Then: Should return empty string for message
        mockMvc.perform(get("/api/webhooks/payment/verify")
                .param("checkoutId", "checkout_no_msg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(""));
    }

    // ========================================
    // Test Group 3: Health Check
    // ========================================

    @Test
    @Order(15)
    @DisplayName("GET /api/webhooks/payment/health - Should return healthy status")
    void testHealthCheck() throws Exception {
        // When/Then: GET /api/webhooks/payment/health
        mockMvc.perform(get("/api/webhooks/payment/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("payment-webhook"))
                .andExpect(jsonPath("$.provider").value("accept-paymob"));
    }

    // ========================================
    // Test Group 4: Edge Cases
    // ========================================

    @Test
    @Order(16)
    @DisplayName("GET /api/webhooks/payment/callback - Should handle very long checkout ID")
    void testCallbackVeryLongCheckoutId() throws Exception {
        // Given: Very long checkout ID
        String longCheckoutId = "a".repeat(500);
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_123",
            PaymentStatus.SUCCESS,
            "Success"
        );
        
        when(paymentPort.verifyPayment(longCheckoutId)).thenReturn(mockResult);

        // When/Then: Should handle gracefully
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", longCheckoutId))
                .andExpect(status().is3xxRedirection());

        verify(paymentPort, times(1)).verifyPayment(longCheckoutId);
    }

    @Test
    @Order(17)
    @DisplayName("GET /api/webhooks/payment/callback - Should handle special characters in checkout ID")
    void testCallbackSpecialCharacters() throws Exception {
        // Given: Checkout ID with special characters
        String checkoutId = "key_!@#$%^&*()_123";
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_123",
            PaymentStatus.SUCCESS,
            "Success"
        );
        
        when(paymentPort.verifyPayment(checkoutId)).thenReturn(mockResult);

        // When/Then: Should handle gracefully
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", checkoutId))
                .andExpect(status().is3xxRedirection());

        verify(paymentPort, times(1)).verifyPayment(checkoutId);
    }

    @Test
    @Order(18)
    @DisplayName("GET /api/webhooks/payment/verify - Should handle special characters in checkout ID")
    void testVerifyUrlEncodedCheckoutId() throws Exception {
        // Given: checkout ID with special characters
        String checkoutId = "key-with-dashes_123";
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_123",
            PaymentStatus.SUCCESS,
            "Success"
        );
        
        when(paymentPort.verifyPayment(checkoutId)).thenReturn(mockResult);

        // When/Then: Should handle special characters
        mockMvc.perform(get("/api/webhooks/payment/verify")
                .param("checkoutId", checkoutId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ========================================
    // Test Group 5: Multiple Query Parameters
    // ========================================

    @Test
    @Order(19)
    @DisplayName("GET /api/webhooks/payment/callback - Should handle extra query parameters")
    void testCallbackExtraParameters() throws Exception {
        // Given: Multiple query parameters (Accept may send extra params)
        PaymentVerificationResult mockResult = new PaymentVerificationResult(
            true,
            "txn_123",
            PaymentStatus.SUCCESS,
            "Success"
        );
        
        when(paymentPort.verifyPayment("key_123")).thenReturn(mockResult);

        // When/Then: Should ignore extra parameters
        mockMvc.perform(get("/api/webhooks/payment/callback")
                .param("id", "key_123")
                .param("resourcePath", "/path/to/resource")
                .param("amount", "100.00")
                .param("currency", "EGP")
                .param("timestamp", "2026-03-01T10:00:00Z"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment/success*"));

        verify(paymentPort, times(1)).verifyPayment("key_123");
    }

    @Test
    @Order(20)
    @DisplayName("GET /api/webhooks/payment/verify - Should require checkoutId parameter")
    void testVerifyMissingCheckoutId() throws Exception {
        // When/Then: GET without checkoutId parameter should fail
        mockMvc.perform(get("/api/webhooks/payment/verify"))
                .andExpect(status().isBadRequest());

        verify(paymentPort, never()).verifyPayment(any());
    }
}
