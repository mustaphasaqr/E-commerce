package com.mustapha.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.jpa.defer-datasource-initialization=false"
})
@DisplayName("Complete Security Stack Integration Tests")
class SecurityStackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    private String validToken;
    private String userId;

    @BeforeEach
    void setUp() {
        User testUser = User.create(
            Username.of("testuser"),
            Email.of("testuser@example.com"),
            Password.fromPlainText("SecurePass123!@#", passwordHasher),
            Role.CUSTOMER
        );
        User savedUser = userRepository.save(testUser);
        userId = savedUser.getId().getValue().toString();
        validToken = jwtTokenGenerator.generateAccessToken(
            userId, 
            savedUser.getRole().name(), 
            "test-session-id"
        );
    }

    @Nested
    @DisplayName("Security Headers Tests")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should include all security headers in response")
        void includeAllSecurityHeaders() throws Exception {
            mockMvc.perform(get("/api/v1/products").secure(true))
                   .andExpect(header().exists("Content-Security-Policy"))
                   .andExpect(header().exists("X-Frame-Options"))
                   .andExpect(header().exists("X-Content-Type-Options"))
                   .andExpect(header().exists("Strict-Transport-Security"))
                   .andExpect(header().exists("Permissions-Policy"))
                   .andExpect(header().string("X-Frame-Options", "DENY"));
        }

        @Test
        @DisplayName("Should include CSP header with proper directives")
        void includeCspWithProperDirectives() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                   .andExpect(header().string("Content-Security-Policy", 
                             matchesPattern(".*default-src 'self'.*")));
        }

        @Test
        @DisplayName("Should include HSTS header")
        void includeHstsHeader() throws Exception {
            mockMvc.perform(get("/api/v1/products").secure(true))
                   .andExpect(header().exists("Strict-Transport-Security"))
                   .andExpect(header().string("Strict-Transport-Security", 
                             matchesPattern(".*max-age=.*")));
        }
    }

    @Nested
    @DisplayName("Request ID Tests")
    class RequestIdTests {

        @Test
        @DisplayName("Should generate and include X-Request-ID header")
        void generateAndIncludeRequestId() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                   .andExpect(header().exists("X-Request-ID"))
                   .andExpect(header().string("X-Request-ID", 
                             matchesPattern("[0-9a-f-]{36}")));
        }

        @Test
        @DisplayName("Should preserve custom request ID")
        void preserveCustomRequestId() throws Exception {
            String customId = "custom-integration-test-id";

            mockMvc.perform(get("/api/v1/products")
                           .header("X-Request-ID", customId))
                   .andExpect(header().string("X-Request-ID", customId));
        }

        @Test
        @DisplayName("Should include request ID in error responses")
        void includeRequestIdInErrors() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content("{\"identifier\":\"wrong\",\"password\":\"wrong\"}"))
                   .andExpect(header().exists("X-Request-ID"));
        }
    }

    @Nested
    @DisplayName("Authentication Integration Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should authenticate valid JWT token")
        void authenticateValidToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                           .header("Authorization", "Bearer " + validToken))
                   .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject invalid JWT token")
        void rejectInvalidToken() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                           .header("Authorization", "Bearer invalid-token"))
                   .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject missing authorization header")
        void rejectMissingAuth() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                   .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should include proper error code for auth failures")
        void includeErrorCodeForAuthFailures() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                           .header("Authorization", "Bearer invalid"))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.errorCode").exists());
        }
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Should allow requests under rate limit")
        void allowRequestsUnderLimit() throws Exception {
            for (int i = 0; i < 10; i++) {
                mockMvc.perform(get("/api/v1/products"))
                       .andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("Should track requests per IP")
        void trackRequestsPerIp() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                           .header("X-Forwarded-For", "192.168.1.100"))
                   .andExpect(status().isOk())
                   .andExpect(header().exists("X-Request-ID"));
        }
    }

    @Nested
    @DisplayName("Error Handling Integration Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return standardized error response")
        void returnStandardizedError() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content("{\"identifier\":\"wrong\",\"password\":\"wrong\"}"))
                   .andExpect(jsonPath("$.timestamp").exists())
                   .andExpect(jsonPath("$.errorCode").exists())
                   .andExpect(jsonPath("$.message").exists())
                   .andExpect(jsonPath("$.path").exists());
        }

        @Test
        @DisplayName("Should not leak sensitive information in errors")
        void notLeakSensitiveInfo() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content("{\"identifier\":\"test@example.com\",\"password\":\"wrong\"}"))
                   .andExpect(jsonPath("$.message").value(
                             org.hamcrest.Matchers.not(
                                 org.hamcrest.Matchers.containsString("SQL")
                             )))
                   .andExpect(jsonPath("$.message").value(
                             org.hamcrest.Matchers.not(
                                 org.hamcrest.Matchers.containsString("database")
                             )));
        }

        @Test
        @DisplayName("Should include X-Request-ID in all error responses")
        void includeRequestIdInAllErrors() throws Exception {
            mockMvc.perform(get("/api/nonexistent-endpoint"))
                   .andExpect(header().exists("X-Request-ID"));
        }
    }

    @Nested
    @DisplayName("Complete Request Flow Tests")
    class CompleteFlowTests {

        @Test
        @DisplayName("Should process authenticated request with all security features")
        void processAuthenticatedRequestComplete() throws Exception {
            String customRequestId = "flow-test-request-id";

            mockMvc.perform(get("/api/v1/users/me")
                           .header("Authorization", "Bearer " + validToken)
                           .header("X-Request-ID", customRequestId))
                   .andExpect(status().isOk())
                   .andExpect(header().string("X-Request-ID", customRequestId))
                   .andExpect(header().exists("Content-Security-Policy"))
                   .andExpect(header().exists("X-Frame-Options"))
                   .andExpect(header().exists("X-Content-Type-Options"));
        }

        @Test
        @DisplayName("Should handle unauthenticated request with security headers")
        void handleUnauthenticatedWithSecurity() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                   .andExpect(status().isOk())
                   .andExpect(header().exists("X-Request-ID"))
                   .andExpect(header().exists("Content-Security-Policy"));
        }

        @Test
        @DisplayName("Should handle POST request with all security layers")
        void handlePostWithAllLayers() throws Exception {
            String loginJson = objectMapper.writeValueAsString(
                new LoginRequest("testuser@example.com", "Password123!@#")
            );

            mockMvc.perform(post("/api/v1/auth/login")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(loginJson))
                   .andExpect(header().exists("X-Request-ID"))
                   .andExpect(header().exists("Content-Security-Policy"));
        }
    }

    @Nested
    @DisplayName("Account Lockout Integration Tests")
    class AccountLockoutTests {

        @Test
        @DisplayName("Should successfully login with correct credentials")
        void loginWithCorrectCredentials() throws Exception {
            String loginJson = "{\"email\":\"testuser@example.com\",\"password\":\"SecurePass123!@#\"}";

            mockMvc.perform(post("/api/v1/auth/login")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(loginJson))
                   .andDo(result -> {
                       System.out.println("Status: " + result.getResponse().getStatus());
                       System.out.println("Response: " + result.getResponse().getContentAsString());
                   })
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.accessToken").exists());
        }

        @Test
        @DisplayName("Should track failed login attempts and return 401")
        void trackFailedAttempts() throws Exception {
            String loginJson = "{\"email\":\"testuser@example.com\",\"password\":\"WrongPassword\"}";

            // Wrong credentials should return 401 UNAUTHORIZED with appropriate error code
            mockMvc.perform(post("/api/v1/auth/login")
                               .contentType(MediaType.APPLICATION_JSON)
                           .content(loginJson))
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_001"))
                   .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should include request ID in lockout response")
        void includeRequestIdInLockout() throws Exception {
            User lockoutUser = User.create(
                Username.of("lockoutuser"),
                Email.of("lockout@example.com"),
                Password.fromPlainText("ValidPass123!@#", passwordHasher),
                Role.CUSTOMER
            );
            userRepository.save(lockoutUser);

            String loginJson = "{\"email\":\"lockout@example.com\",\"password\":\"WrongPassword\"}";

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(loginJson))
                       .andExpect(header().exists("X-Request-ID"));
            }
        }
    }

    // Helper DTOs
    private record LoginRequest(String identifier, String password) {}
}
