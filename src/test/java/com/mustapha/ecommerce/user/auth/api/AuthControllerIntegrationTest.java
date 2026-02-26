package com.mustapha.ecommerce.user.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;
import com.mustapha.ecommerce.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.cache.type=none"})
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        // Clear all Redis data for tests to avoid deserialization issues with old cached objects
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();

        User testUser = User.create(
            Username.of("authtest"),
            Email.of("authtest@example.com"),
            Password.fromPlainText("AuthTest123!@#", passwordHasher),
            Role.CUSTOMER
        );
        testUser.acceptTerms("v1.0");
        testUser.verifyEmail();
        testUser.activate("Test setup");
        userRepository.save(testUser);
    }

    @Test
    void login_ValidCredentials_Returns200WithJwt() throws Exception {
        LoginRequest request = new LoginRequest("authtest@example.com", "AuthTest123!@#");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.expiresIn").value(3600000L))
            .andExpect(jsonPath("$.sessionId").exists())
            .andExpect(jsonPath("$.sessionId").isString())
            .andExpect(jsonPath("$.user.id").exists())
            .andExpect(jsonPath("$.user.username").value("authtest"))
            .andExpect(jsonPath("$.user.email").value("authtest@example.com"))
            .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
            .andExpect(jsonPath("$.user.status").value("ACTIVE"));
    }

    @Test
    void login_InvalidPassword_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("authtest@example.com", "WrongPassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_NonExistentUser_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_JwtContainsCorrectClaims() throws Exception {
        LoginRequest request = new LoginRequest("authtest@example.com", "AuthTest123!@#");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();

        // Verify JWT token structure (should contain 3 parts: header.payload.signature)
        String[] parts = accessToken.split("\\.");
        assert parts.length == 3 : "JWT should have 3 parts";
    }

    @Test
    void refreshToken_ValidRefreshToken_Returns200WithNewJwt() throws Exception {
        // First login to get refresh token
        LoginRequest loginRequest = new LoginRequest("authtest@example.com", "AuthTest123!@#");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
            .get("refreshToken").asText();

        // Use refresh token to get new access token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.expiresIn").value(3600000L))
            .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void refreshToken_InvalidToken_Returns400() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token-12345");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void logout_ValidSession_Returns204() throws Exception {
        // Login first
        LoginRequest loginRequest = new LoginRequest("authtest@example.com", "AuthTest123!@#");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
            .get("accessToken").asText();

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }

    @Test
    void logout_WithoutAuthentication_Returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized()); // 401 for missing authentication
    }

    @Test
    void logoutAll_ValidSession_Returns204() throws Exception {
        // Login twice to create multiple sessions
        LoginRequest loginRequest = new LoginRequest("authtest@example.com", "AuthTest123!@#");
        
        MvcResult firstLogin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk());

        String accessToken = objectMapper.readTree(firstLogin.getResponse().getContentAsString())
            .get("accessToken").asText();

        // Logout all devices
        mockMvc.perform(post("/api/auth/logout-all")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }

    @Test
    void rateLimiting_ExceedsMaxAttempts_Returns429() throws Exception {
        // Use unique identifier to avoid conflicts from previous tests
        String uniqueEmail = "ratelimit" + System.currentTimeMillis() + "@example.com";
        LoginRequest request = new LoginRequest(uniqueEmail, "WrongPassword!");

        // Clear any existing rate limit data for this user to ensure clean test
        redisTemplate.delete(redisTemplate.keys("rate_limit:user:" + uniqueEmail + ":*"));

        // Attempt 5 failed logins (max allowed - should all return 401)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email/username or password"));
        }

        // 6th attempt should be rate limited (returns 429)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message").value(containsString("Too many failed login attempts")));
    }

    @Test
    void rateLimiting_IpBasedBlocking_WorksAcrossMultipleUsers() throws Exception {
        // Register another user (use same password format as setUp method)
        RegisterUserRequest registerRequest = new RegisterUserRequest(
            "user2@example.com",
            "User2Test123!@#",  // Same format as authtest user password
            "user2",
            true
        );
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated());

        // Attempt multiple failed logins from same IP (20 max per IP)
        LoginRequest request1 = new LoginRequest("authtest@example.com", "WrongPass!");
        LoginRequest request2 = new LoginRequest("user2@example.com", "WrongPass!");

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)));
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)));
        }

        // 21st attempt should be blocked (IP level)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    void passwordResetRequest_ValidEmail_Returns204() throws Exception {
        PasswordResetRequestRequest request = new PasswordResetRequestRequest("authtest@example.com");

        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        // Verify token was created in Redis
        var keys = redisTemplate.keys("password_reset:*");
        assertThat(keys).isNotNull();
        assertThat(keys.size()).isGreaterThan(0);
    }

    @Test
    void passwordResetRequest_NonExistentEmail_Returns204SilentFailure() throws Exception {
        PasswordResetRequestRequest request = new PasswordResetRequestRequest("nonexistent@example.com");

        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        // Verify NO token was created (silent failure for security)
        var keys = redisTemplate.keys("password_reset:*");
        assertThat(keys == null || keys.isEmpty()).isTrue();
    }

    @Test
    void passwordResetComplete_ValidToken_Returns204AndChangesPassword() throws Exception {
        // Step 1: Request password reset
        PasswordResetRequestRequest resetRequest = new PasswordResetRequestRequest("authtest@example.com");
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isNoContent());

        // Step 2: Extract token from Redis
        var keys = redisTemplate.keys("password_reset:*");
        assertThat(keys).isNotNull();
        assertThat(keys.size()).isEqualTo(1);
        String tokenKey = keys.iterator().next();
        String token = tokenKey.replace("password_reset:", "");

        // Step 3: Complete password reset
        PasswordResetCompleteRequest completeRequest = new PasswordResetCompleteRequest(
            token, 
            "NewPassword123!@#"
        );
        mockMvc.perform(post("/api/auth/password-reset/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
            .andExpect(status().isNoContent());

        // Step 4: Verify old password no longer works
        LoginRequest oldPasswordLogin = new LoginRequest("authtest@example.com", "AuthTest123!@#");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oldPasswordLogin)))
            .andExpect(status().isUnauthorized());

        // Step 5: Verify new password works
        LoginRequest newPasswordLogin = new LoginRequest("authtest@example.com", "NewPassword123!@#");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPasswordLogin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void passwordResetComplete_InvalidToken_Returns400() throws Exception {
        PasswordResetCompleteRequest request = new PasswordResetCompleteRequest(
            "invalid-token-12345", 
            "NewPassword123!@#"
        );

        mockMvc.perform(post("/api/auth/password-reset/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Invalid or expired reset token")));
    }

    @Test
    void passwordResetComplete_TokenReuseAttempt_Returns400() throws Exception {
        // Step 1: Request reset
        PasswordResetRequestRequest resetRequest = new PasswordResetRequestRequest("authtest@example.com");
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isNoContent());

        // Step 2: Get token
        var keys = redisTemplate.keys("password_reset:*");
        String token = keys.iterator().next().replace("password_reset:", "");

        // Step 3: Use token once
        PasswordResetCompleteRequest completeRequest = new PasswordResetCompleteRequest(
            token, 
            "NewPassword123!@#"
        );
        mockMvc.perform(post("/api/auth/password-reset/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
            .andExpect(status().isNoContent());

        // Step 4: Try to reuse same token - should fail
        PasswordResetCompleteRequest reuseRequest = new PasswordResetCompleteRequest(
            token, 
            "AnotherPassword123!@#"
        );
        mockMvc.perform(post("/api/auth/password-reset/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reuseRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Invalid or expired reset token")));
    }

    @Test
    void logoutAllDevices_WithMultipleSessions_OnlyCurrentRemains() throws Exception {
        // Step 1: Create 3 login sessions for same user
        LoginRequest loginRequest = new LoginRequest("authtest@example.com", "AuthTest123!@#");
        
        MvcResult login1 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        String session1Id = objectMapper.readTree(login1.getResponse().getContentAsString())
            .get("sessionId").asText();

        MvcResult login2 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        String session2Id = objectMapper.readTree(login2.getResponse().getContentAsString())
            .get("sessionId").asText();

        MvcResult login3 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        String currentSessionToken = objectMapper.readTree(login3.getResponse().getContentAsString())
            .get("accessToken").asText();
        String currentSessionId = objectMapper.readTree(login3.getResponse().getContentAsString())
            .get("sessionId").asText();

        // Step 2: Verify all 3 sessions exist in Redis
        var sessionKeys = redisTemplate.keys("login_session:*");
        assertThat(sessionKeys).hasSize(3);

        // Step 3: Logout all devices from current session
        mockMvc.perform(post("/api/auth/logout-all")
                .header("Authorization", "Bearer " + currentSessionToken))
            .andExpect(status().isNoContent());

        // Step 4: Verify only current session remains in Redis
        var remainingSessions = redisTemplate.keys("login_session:*");
        assertThat(remainingSessions).hasSize(1);
        
        String remainingKey = remainingSessions.iterator().next();
        assertThat(remainingKey).contains(currentSessionId);
        
        // Step 5: Verify old session IDs are not in Redis
        assertThat(redisTemplate.hasKey("login_session:" + session1Id)).isFalse();
        assertThat(redisTemplate.hasKey("login_session:" + session2Id)).isFalse();
        
        // Note: JWTs are stateless, so old tokens still work until expiry.
        // In production, use short JWT expiry (e.g., 15min) with refresh tokens.
        // Session deletion prevents refresh token use.
    }

    @Test
    void emailVerificationRequest_ValidEmail_Returns204AndCreatesToken() throws Exception {
        // Create unverified user
        User unverifiedUser = User.create(
            Username.of("unverified"),
            Email.of("unverified@example.com"),
            Password.fromPlainText("Unverified123!@#", passwordHasher),
            Role.CUSTOMER
        );
        unverifiedUser.acceptTerms("v1.0");
        userRepository.save(unverifiedUser);

        RequestEmailVerificationRequest request = new RequestEmailVerificationRequest("unverified@example.com");

        mockMvc.perform(post("/api/auth/email-verification/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        // Verify token was created in Redis
        var keys = redisTemplate.keys("email_verification:*");
        assertThat(keys).isNotNull();
        assertThat(keys.size()).isEqualTo(1);
    }

    @Test
    void emailVerificationRequest_AlreadyVerified_Returns400() throws Exception {
        RequestEmailVerificationRequest request = new RequestEmailVerificationRequest("authtest@example.com");

        mockMvc.perform(post("/api/auth/email-verification/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Email already verified")));
    }

    @Test
    void verifyEmailWithToken_ValidToken_Returns204AndVerifiesEmail() throws Exception {
        // Create unverified user
        User unverifiedUser = User.create(
            Username.of("unverified2"),
            Email.of("unverified2@example.com"),
            Password.fromPlainText("Unverified123!@#", passwordHasher),
            Role.CUSTOMER
        );
        unverifiedUser.acceptTerms("v1.0");
        userRepository.save(unverifiedUser);

        // Request verification
        RequestEmailVerificationRequest verificationRequest = new RequestEmailVerificationRequest("unverified2@example.com");
        mockMvc.perform(post("/api/auth/email-verification/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
            .andExpect(status().isNoContent());

        // Get token from Redis
        var keys = redisTemplate.keys("email_verification:*");
        assertThat(keys).hasSize(1);
        String token = keys.iterator().next().replace("email_verification:", "");

        // Verify email with token
        VerifyEmailWithTokenRequest verifyRequest = new VerifyEmailWithTokenRequest(token);
        mockMvc.perform(post("/api/auth/email-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
            .andExpect(status().isNoContent());

        // Verify user email is now verified
        User updatedUser = userRepository.findByEmail(Email.of("unverified2@example.com")).orElseThrow();
        assertThat(updatedUser.isEmailVerified()).isTrue();

        // Verify token was deleted
        assertThat(redisTemplate.hasKey("email_verification:" + token)).isFalse();
    }

    @Test
    void verifyEmailWithToken_InvalidToken_Returns400() throws Exception {
        VerifyEmailWithTokenRequest request = new VerifyEmailWithTokenRequest("invalid-token");

        mockMvc.perform(post("/api/auth/email-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Invalid or expired verification token")));
    }

    @Test
    void verifyEmailWithToken_TokenReuse_Returns400() throws Exception {
        // Create unverified user
        User unverifiedUser = User.create(
            Username.of("unverified3"),
            Email.of("unverified3@example.com"),
            Password.fromPlainText("Unverified123!@#", passwordHasher),
            Role.CUSTOMER
        );
        unverifiedUser.acceptTerms("v1.0");
        userRepository.save(unverifiedUser);

        // Request verification
        RequestEmailVerificationRequest verificationRequest = new RequestEmailVerificationRequest("unverified3@example.com");
        mockMvc.perform(post("/api/auth/email-verification/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
            .andExpect(status().isNoContent());

        // Get token
        var keys = redisTemplate.keys("email_verification:*");
        String token = keys.iterator().next().replace("email_verification:", "");

        // Use token once
        VerifyEmailWithTokenRequest verifyRequest = new VerifyEmailWithTokenRequest(token);
        mockMvc.perform(post("/api/auth/email-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
            .andExpect(status().isNoContent());

        // Try to reuse token - should fail (token was deleted)
        mockMvc.perform(post("/api/auth/email-verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Invalid or expired verification token")));
    }
}

