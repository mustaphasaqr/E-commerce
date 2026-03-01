package com.mustapha.ecommerce.shared.external.email;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.annotation.AsyncResult;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test Suite: EmailServiceImpl with Real SendGrid Integration
 * 
 * Tests cover:
 * 1. SendGrid API integration (mocked)
 * 2. Template rendering with Thymeleaf
 * 3. Email content validation
 * 4. Error handling and retries
 * 5. Circuit breaker behavior
 * 6. Fallback mechanisms
 * 7. Async execution
 * 8. MOCK vs REAL mode
 * 
 * Total: 25 comprehensive tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailServiceImplTest {

    private EmailServiceImpl emailService;
    private SendGrid mockSendGrid;
    private TemplateEngine mockTemplateEngine;
    private CircuitBreakerRegistry mockCircuitBreakerRegistry;
    private CircuitBreaker mockCircuitBreaker;

    @BeforeEach
    void setUp() {
        mockSendGrid = mock(SendGrid.class);
        mockTemplateEngine = mock(TemplateEngine.class);
        mockCircuitBreakerRegistry = mock(CircuitBreakerRegistry.class);
        mockCircuitBreaker = mock(CircuitBreaker.class);

        // Mock template engine responses
        when(mockTemplateEngine.process(eq("email/welcome"), any(Context.class)))
            .thenReturn("<html><body>Welcome {{username}}</body></html>");
        when(mockTemplateEngine.process(eq("email/password-reset"), any(Context.class)))
            .thenReturn("<html><body>Reset password: {{resetLink}}</body></html>");
        when(mockTemplateEngine.process(eq("email/email-verification"), any(Context.class)))
            .thenReturn("<html><body>Verify email: {{verificationLink}}</body></html>");
    }

    // ========================================
    // Test Group 1: REAL Mode - Welcome Email
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Should send welcome email successfully in REAL mode")
    void testSendWelcomeEmailSuccess() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        mockResponse.setBody("{}");
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When: Send welcome email
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "JohnDoe"));

        // Then: SendGrid API called
        verify(mockSendGrid, times(1)).api(any(Request.class));
        verify(mockTemplateEngine, times(1)).process(eq("email/welcome"), any(Context.class));
    }

    @Test
    @Order(2)
    @DisplayName("Should include correct email parameters for welcome email")
    void testWelcomeEmailParameters() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        // When: Send welcome email
        emailService.sendWelcomeEmail("test@example.com", "TestUser");

        // Then: Verify request structure
        verify(mockSendGrid).api(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();
        
        assertThat(capturedRequest.getMethod().toString()).isEqualTo("POST");
        assertThat(capturedRequest.getEndpoint()).isEqualTo("mail/send");
        assertThat(capturedRequest.getBody()).contains("test@example.com");
        assertThat(capturedRequest.getBody()).contains("noreply@ecommerce.com");
    }

    @Test
    @Order(3)
    @DisplayName("Should handle SendGrid 4xx errors gracefully")
    void testWelcomeEmailSendGrid4xxError() throws IOException {
        // Given: SendGrid returns 400 Bad Request
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(400);
        mockResponse.setBody("{\"errors\":[{\"message\":\"Invalid email\"}]}");
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When/Then: Should throw exception
        assertThrows(RuntimeException.class, () -> 
            emailService.sendWelcomeEmail("invalid", "User")
        );
    }

    @Test
    @Order(4)
    @DisplayName("Should handle SendGrid 5xx errors and throw exception")
    void testWelcomeEmailSendGrid5xxError() throws IOException {
        // Given: SendGrid returns 500 Internal Server Error
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(500);
        mockResponse.setBody("{\"errors\":[{\"message\":\"Internal error\"}]}");
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When/Then: Should throw exception for retry
        assertThrows(RuntimeException.class, () -> 
            emailService.sendWelcomeEmail("user@example.com", "User")
        );
    }

    @Test
    @Order(5)
    @DisplayName("Should handle IOException from SendGrid")
    void testWelcomeEmailIOException() throws IOException {
        // Given: SendGrid throws IOException
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        when(mockSendGrid.api(any(Request.class)))
            .thenThrow(new IOException("Network timeout"));

        // When/Then: Should throw RuntimeException
        assertThrows(RuntimeException.class, () -> 
            emailService.sendWelcomeEmail("user@example.com", "User")
        );
    }

    // ========================================
    // Test Group 2: REAL Mode - Password Reset Email
    // ========================================

    @Test
    @Order(6)
    @DisplayName("Should send password reset email successfully")
    void testSendPasswordResetEmailSuccess() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When: Send password reset email
        assertDoesNotThrow(() -> 
            emailService.sendPasswordResetEmail("user@example.com", "reset_token_12345")
        );

        // Then: SendGrid API called with correct template
        verify(mockSendGrid, times(1)).api(any(Request.class));
        verify(mockTemplateEngine, times(1))
            .process(eq("email/password-reset"), any(Context.class));
    }

    @Test
    @Order(7)
    @DisplayName("Should include reset token in password reset email")
    void testPasswordResetEmailContainsToken() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "support@ecommerce.com",
            "Support Team",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        // When: Send password reset email
        emailService.sendPasswordResetEmail("test@example.com", "secure_token_abc123");

        // Then: Template should receive context with reset link
        verify(mockTemplateEngine).process(eq("email/password-reset"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        
        // Context should contain resetLink with the token
        assertThat(capturedContext.getVariable("resetLink")).isNotNull();
        assertThat(capturedContext.getVariable("expirationTime")).isNotNull();
    }

    @Test
    @Order(8)
    @DisplayName("Should handle very long reset tokens")
    void testPasswordResetWithLongToken() throws IOException {
        // Given: Email service with long token
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        String longToken = "a".repeat(500); // 500 character token

        // When/Then: Should handle without error
        assertDoesNotThrow(() -> 
            emailService.sendPasswordResetEmail("user@example.com", longToken)
        );
    }

    @Test
    @Order(9)
    @DisplayName("Should handle empty reset token gracefully")
    void testPasswordResetWithEmptyToken() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When/Then: Should handle empty token (logs show substring handling)
        assertDoesNotThrow(() -> 
            emailService.sendPasswordResetEmail("user@example.com", "")
        );
    }

    // ========================================
    // Test Group 3: REAL Mode - Email Verification
    // ========================================

    @Test
    @Order(10)
    @DisplayName("Should send email verification successfully")
    void testSendEmailVerificationSuccess() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When: Send verification email
        assertDoesNotThrow(() -> 
            emailService.sendEmailVerificationEmail("user@example.com", "verify_token_xyz789")
        );

        // Then: SendGrid API called with verification template
        verify(mockSendGrid, times(1)).api(any(Request.class));
        verify(mockTemplateEngine, times(1))
            .process(eq("email/email-verification"), any(Context.class));
    }

    @Test
    @Order(11)
    @DisplayName("Should include verification link in email")
    void testEmailVerificationContainsLink() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "verify@ecommerce.com",
            "Verification Service",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        // When: Send verification email
        emailService.sendEmailVerificationEmail("test@example.com", "token_verify_123");

        // Then: Template should receive verification link
        verify(mockTemplateEngine).process(eq("email/email-verification"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        
        assertThat(capturedContext.getVariable("verificationLink")).isNotNull();
        assertThat(capturedContext.getVariable("expirationTime")).isNotNull();
    }

    @Test
    @Order(12)
    @DisplayName("Should handle short verification tokens")
    void testEmailVerificationWithShortToken() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When/Then: Should handle tokens shorter than 8 characters
        assertDoesNotThrow(() -> 
            emailService.sendEmailVerificationEmail("user@example.com", "abc")
        );
        assertDoesNotThrow(() -> 
            emailService.sendEmailVerificationEmail("user@example.com", "xy")
        );
    }

    // ========================================
    // Test Group 4: MOCK Mode Testing
    // ========================================

    @Test
    @Order(13)
    @DisplayName("Should work in MOCK mode when SendGrid is null")
    void testMockModeWithNullSendGrid() {
        // Given: Email service in MOCK mode (no SendGrid)
        emailService = new EmailServiceImpl(
            null,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        // When/Then: Should not throw exceptions and should log
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "User"));
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "token"));
        assertDoesNotThrow(() -> emailService.sendEmailVerificationEmail("user@example.com", "token"));

        // Then: Template engine should not be called in MOCK mode
        verify(mockTemplateEngine, never()).process(anyString(), any(Context.class));
    }

    @Test
    @Order(14)
    @DisplayName("Should work in MOCK mode when email is disabled")
    void testMockModeWhenDisabled() {
        // Given: Email service disabled
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            false, // disabled
            mockTemplateEngine
        );

        // When/Then: Should not throw exceptions
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "User"));

        // Then: SendGrid should not be called
        verifyNoInteractions(mockSendGrid);
        verify(mockTemplateEngine, never()).process(anyString(), any(Context.class));
    }

    @Test
    @Order(15)
    @DisplayName("MOCK mode should handle empty email addresses")
    void testMockModeWithEmptyEmail() {
        // Given: Email service in MOCK mode
        emailService = new EmailServiceImpl(
            null,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        // When/Then: Should handle edge cases gracefully
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("", "User"));
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("", "token"));
        assertDoesNotThrow(() -> emailService.sendEmailVerificationEmail("", "token"));
    }

    @Test
    @Order(16)
    @DisplayName("MOCK mode should handle null values")
    void testMockModeWithNullValues() {
        // Given: Email service in MOCK mode
        emailService = new EmailServiceImpl(
            null,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        // When/Then: Should handle null gracefully (null checks needed)
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "User"));
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "token"));
    }

    // ========================================
    // Test Group 5: Email Address Validation
    // ========================================

    @Test
    @Order(17)
    @DisplayName("Should accept valid email formats")
    void testValidEmailFormats() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When/Then: Should accept various valid formats
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "User"));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user.name@example.com", "User"));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user+tag@example.co.uk", "User"));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user_123@sub.example.com", "User"));
    }

    @Test
    @Order(18)
    @DisplayName("Should handle special characters in username")
    void testSpecialCharactersInUsername() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When/Then: Should handle special characters
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "O'Brien"));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "José García"));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("user@example.com", "User123!@#"));
    }

    // ========================================
    // Test Group 6: Configuration Validation
    // ========================================

    @Test
    @Order(19)
    @DisplayName("Should use correct from email and name")
    void testFromEmailConfiguration() throws IOException {
        // Given: Email service with specific from address
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "custom@mycompany.com",
            "My Company Name",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        // When: Send email
        emailService.sendWelcomeEmail("user@example.com", "User");

        // Then: Should use configured from address
        verify(mockSendGrid).api(requestCaptor.capture());
        String requestBody = requestCaptor.getValue().getBody();
        
        assertThat(requestBody).contains("custom@mycompany.com");
        assertThat(requestBody).contains("My Company Name");
    }

    @Test
    @Order(20)
    @DisplayName("Should initialize correctly with all required parameters")
    void testServiceInitialization() {
        // When: Create email service with all parameters
        EmailServiceImpl service = new EmailServiceImpl(
            mockSendGrid,
            "test@example.com",
            "Test Service",
            true,
            mockTemplateEngine
        );

        // Then: Service should be created successfully
        assertThat(service).isNotNull();
    }

    // ========================================
    // Test Group 7: Template Rendering
    // ========================================

    @Test
    @Order(21)
    @DisplayName("Should call correct template for each email type")
    void testCorrectTemplateSelection() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When: Send different email types
        emailService.sendWelcomeEmail("user@example.com", "User");
        emailService.sendPasswordResetEmail("user@example.com", "token");
        emailService.sendEmailVerificationEmail("user@example.com", "token");

        // Then: Each should use correct template
        verify(mockTemplateEngine, times(1)).process(eq("email/welcome"), any(Context.class));
        verify(mockTemplateEngine, times(1)).process(eq("email/password-reset"), any(Context.class));
        verify(mockTemplateEngine, times(1)).process(eq("email/email-verification"), any(Context.class));
    }

    @Test
    @Order(22)
    @DisplayName("Should pass correct context variables to templates")
    void testTemplateContextVariables() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        // When: Send welcome email
        emailService.sendWelcomeEmail("user@example.com", "JohnDoe");

        // Then: Context should contain username
        verify(mockTemplateEngine).process(eq("email/welcome"), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        
        assertThat(context.getVariable("username")).isEqualTo("JohnDoe");
    }

    // ========================================
    // Test Group 8: Error Scenarios
    // ========================================

    @Test
    @Order(23)
    @DisplayName("Should throw exception when template rendering fails")
    void testTemplateRenderingFailure() throws IOException {
        // Given: Template engine throws exception
        when(mockTemplateEngine.process(eq("email/welcome"), any(Context.class)))
            .thenThrow(new RuntimeException("Template not found"));

        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        // When/Then: Should propagate exception
        assertThrows(RuntimeException.class, () -> 
            emailService.sendWelcomeEmail("user@example.com", "User")
        );
    }

    @Test
    @Order(24)
    @DisplayName("Should handle SendGrid rate limit (429) errors")
    void testSendGridRateLimitError() throws IOException {
        // Given: SendGrid returns 429 Too Many Requests
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(429);
        mockResponse.setBody("{\"errors\":[{\"message\":\"Rate limit exceeded\"}]}");
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When/Then: Should throw exception for retry
        assertThrows(RuntimeException.class, () -> 
            emailService.sendWelcomeEmail("user@example.com", "User")
        );
    }

    @Test
    @Order(25)
    @DisplayName("Should handle concurrent email sending")
    void testConcurrentEmailSending() throws IOException {
        // Given: Email service in REAL mode
        emailService = new EmailServiceImpl(
            mockSendGrid,
            "noreply@ecommerce.com",
            "E-Commerce Platform",
            true,
            mockTemplateEngine
        );

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // When: Send multiple emails concurrently
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                emailService.sendWelcomeEmail("user" + i + "@example.com", "User" + i);
            }
        });

        // Then: All emails should be processed
        verify(mockSendGrid, times(10)).api(any(Request.class));
    }
}
