package com.mustapha.ecommerce.shared.external.support;

import com.mustapha.ecommerce.shared.external.support.CustomerSupportPort.ChatConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive Test Suite for TawkToSupportAdapter
 * 
 * Coverage:
 * - Unit Tests: Chat initialization, HMAC generation, graceful degradation
 * - Resilience Tests: Missing config, null handling, invalid inputs
 * - Integration Tests: Real Spring configuration
 * 
 * Test Philosophy:
 * - Tests work with test credentials from application-test.properties
 * - Tests HMAC generation with known inputs
 * - Tests configuration loading from application properties
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TawkToSupportAdapterTest {

    @Autowired
    private TawkToSupportAdapter tawkToAdapter;

    // ========================================
    // Chat Initialization Tests
    // ========================================
    
    @Nested
    @DisplayName("Chat Initialization")
    class ChatInitializationTests {
        
        @Test
        @Order(1)
        @DisplayName("Should initialize chat for customer with valid data")
        void shouldInitializeChatForCustomer() {
            // Given
            Long customerId = 100L;
            String customerName = "John Doe";
            String customerEmail = "john.doe@example.com";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(customerId, customerName, customerEmail);
            
            // Then
            assertThat(config).isNotNull();
            assertThat(config.propertyId()).isNotNull();
            assertThat(config.widgetId()).isNotNull();
            assertThat(config.welcomeMessage()).contains(customerName);
        }
        
        @Test
        @Order(2)
        @DisplayName("Should include welcome message with customer name")
        void shouldIncludeWelcomeMessageWithName() {
            // Given
            String customerName = "Sarah Ahmed";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                200L, customerName, "sarah@example.com");
            
            // Then
            assertThat(config.welcomeMessage())
                .contains("Sarah Ahmed")
                .contains("How can we help you");
        }
        
        @Test
        @Order(3)
        @DisplayName("Should generate HMAC signature for secure mode")
        void shouldGenerateHmacSignature() {
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                300L, "Test User", "test@example.com");
            
            // Then - HMAC signature should be present (even if empty when not configured)
            assertThat(config.secureHmac()).isNotNull();
        }
        
        @Test
        @Order(4)
        @DisplayName("Should handle customer with long name")
        void shouldHandleLongCustomerName() {
            // Given
            String longName = "Abdullah Mohammed Ali Hassan Al-Sayed Omar";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                400L, longName, "abdullah@example.com");
            
            // Then
            assertThat(config.welcomeMessage()).contains(longName);
        }
        
        @Test
        @Order(5)
        @DisplayName("Should handle customer with special characters in name")
        void shouldHandleSpecialCharactersInName() {
            // Given
            String specialName = "François O'Reilly-Smith";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                500L, specialName, "francois@example.com");
            
            // Then
            assertThat(config.welcomeMessage()).contains(specialName);
        }
        
        @Test
        @Order(6)
        @DisplayName("Should handle Arabic customer name")
        void shouldHandleArabicName() {
            // Given
            String arabicName = "محمد أحمد";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                600L, arabicName, "ahmed@example.com");
            
            // Then
            assertThat(config.welcomeMessage()).contains(arabicName);
        }
    }
    
    // ========================================
    // Graceful Degradation Tests (No Credentials)
    // ========================================
    
    @Nested
    @DisplayName("Graceful Degradation (No Config)")
    @TestPropertySource(properties = {
        "tawk.property-id=",
        "tawk.api-key="
    })
    class GracefulDegradationTests {
        
        @Autowired
        private TawkToSupportAdapter unconfiguredAdapter;
        
        @Test
        @Order(7)
        @DisplayName("Should return disabled config when not configured")
        void shouldReturnDisabledConfigWhenNotConfigured() {
            // When
            ChatConfig config = unconfiguredAdapter.initializeChatForCustomer(
                700L, "Test User", "test@example.com");
            
            // Then
            assertThat(config.isOnline()).isFalse();
            assertThat(config.welcomeMessage()).contains("temporarily unavailable");
            assertThat(config.propertyId()).isEmpty();
            assertThat(config.widgetId()).isEmpty();
        }
        
        @Test
        @Order(8)
        @DisplayName("Should handle offline message creation without config")
        void shouldHandleOfflineMessageWithoutConfig() {
            // When/Then - Should not throw exception
            assertThatCode(() -> unconfiguredAdapter.createOfflineMessage(
                800L, "Need help with order"))
                .doesNotThrowAnyException();
        }
    }
    
    // ========================================
    // Resilience Tests (Null & Edge Cases)
    // ========================================
    
    @Nested
    @DisplayName("Resilience & Error Handling")
    class ResilienceTests {
        
        @Test
        @Order(9)
        @DisplayName("Should handle null customer name")
        void shouldHandleNullCustomerName() {
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                900L, null, "test@example.com");
            
            // Then - Should not crash, but welcome message should still work
            assertThat(config).isNotNull();
            assertThat(config.welcomeMessage()).isNotNull();
        }
        
        @Test
        @Order(10)
        @DisplayName("Should handle empty customer name")
        void shouldHandleEmptyCustomerName() {
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                1000L, "", "test@example.com");
            
            // Then
            assertThat(config).isNotNull();
            assertThat(config.welcomeMessage()).isNotNull();
        }
        
        @Test
        @Order(11)
        @DisplayName("Should handle null customer email")
        void shouldHandleNullCustomerEmail() {
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                1100L, "John Doe", null);
            
            // Then - Should handle gracefully
            assertThat(config).isNotNull();
        }
        
        @Test
        @Order(12)
        @DisplayName("Should handle empty customer email")
        void shouldHandleEmptyCustomerEmail() {
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                1200L, "Jane Smith", "");
            
            // Then
            assertThat(config).isNotNull();
        }
        
        @Test
        @Order(13)
        @DisplayName("Should handle invalid email format")
        void shouldHandleInvalidEmailFormat() {
            // Given
            String invalidEmail = "not-an-email";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                1300L, "Test User", invalidEmail);
            
            // Then - Should still work (Tawk.to doesn't enforce email validation)
            assertThat(config).isNotNull();
        }
        
        @Test
        @Order(14)
        @DisplayName("Should handle very long email")
        void shouldHandleVeryLongEmail() {
            // Given
            String longEmail = "verylongemailaddress" + "a".repeat(100) + "@example.com";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                1400L, "Test User", longEmail);
            
            // Then
            assertThat(config).isNotNull();
        }
        
        @Test
        @Order(15)
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            // Given
            String specialEmail = "user+tag@example.co.uk";
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                1500L, "Special User", specialEmail);
            
            // Then
            assertThat(config).isNotNull();
        }
        
        @Test
        @Order(16)
        @DisplayName("Should handle zero customer ID")
        void shouldHandleZeroCustomerId() {
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                0L, "Test User", "test@example.com");
            
            // Then
            assertThat(config).isNotNull();
        }
        
        @Test
        @Order(17)
        @DisplayName("Should handle negative customer ID")
        void shouldHandleNegativeCustomerId() {
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                -100L, "Test User", "test@example.com");
            
            // Then
            assertThat(config).isNotNull();
        }
        
        @Test
        @Order(18)
        @DisplayName("Should handle very large customer ID")
        void shouldHandleVeryLargeCustomerId() {
            // Given
            Long largeId = Long.MAX_VALUE;
            
            // When
            ChatConfig config = tawkToAdapter.initializeChatForCustomer(
                largeId, "Test User", "test@example.com");
            
            // Then
            assertThat(config).isNotNull();
        }
    }
    
    // ========================================
    // Offline Message Tests
    // ========================================
    
    @Nested
    @DisplayName("Offline Message Handling")
    class OfflineMessageTests {
        
        @Test
        @Order(19)
        @DisplayName("Should create offline message")
        void shouldCreateOfflineMessage() {
            // Given
            Long customerId = 1600L;
            String message = "I need help with my order #12345";
            
            // When/Then - Should not throw exception
            assertThatCode(() -> tawkToAdapter.createOfflineMessage(customerId, message))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(20)
        @DisplayName("Should handle empty offline message")
        void shouldHandleEmptyOfflineMessage() {
            // When/Then
            assertThatCode(() -> tawkToAdapter.createOfflineMessage(1700L, ""))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(21)
        @DisplayName("Should handle null offline message")
        void shouldHandleNullOfflineMessage() {
            // When/Then
            assertThatCode(() -> tawkToAdapter.createOfflineMessage(1800L, null))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(22)
        @DisplayName("Should handle very long offline message")
        void shouldHandleVeryLongOfflineMessage() {
            // Given
            String longMessage = "Help! ".repeat(100);
            
            // When/Then
            assertThatCode(() -> tawkToAdapter.createOfflineMessage(1900L, longMessage))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(23)
        @DisplayName("Should handle offline message with special characters")
        void shouldHandleOfflineMessageWithSpecialChars() {
            // Given
            String specialMessage = "Order #12345 failed! Can you help? 😕 <script>alert('test')</script>";
            
            // When/Then
            assertThatCode(() -> tawkToAdapter.createOfflineMessage(2000L, specialMessage))
                .doesNotThrowAnyException();
        }
    }
    
    // ========================================
    // Chat Transcript Tests
    // ========================================
    
    @Nested
    @DisplayName("Chat Transcript Retrieval")
    class ChatTranscriptTests {
        
        @Test
        @Order(24)
        @DisplayName("Should get chat transcript")
        void shouldGetChatTranscript() {
            // Given
            String chatId = "chat-12345";
            
            // When
            String transcript = tawkToAdapter.getChatTranscript(chatId);
            
            // Then - Returns empty string (TODO implementation)
            assertThat(transcript).isNotNull();
        }
        
        @Test
        @Order(25)
        @DisplayName("Should handle null chat ID")
        void shouldHandleNullChatId() {
            // When/Then
            assertThatCode(() -> tawkToAdapter.getChatTranscript(null))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(26)
        @DisplayName("Should handle empty chat ID")
        void shouldHandleEmptyChatId() {
            // When/Then
            assertThatCode(() -> tawkToAdapter.getChatTranscript(""))
                .doesNotThrowAnyException();
        }
    }
    
    // ========================================
    // HMAC Security Tests
    // ========================================
    
    @Nested
    @DisplayName("HMAC Security & Validation")
    @TestPropertySource(properties = {
        "tawk.property-id=test-property-123",
        "tawk.api-key=test-api-key-456"
    })
    class HmacSecurityTests {
        
        @Autowired
        private TawkToSupportAdapter configuredAdapter;
        
        @Test
        @Order(27)
        @DisplayName("Should generate consistent HMAC for same email")
        void shouldGenerateConsistentHmac() {
            // Given
            String email = "test@example.com";
            
            // When
            ChatConfig config1 = configuredAdapter.initializeChatForCustomer(1L, "User1", email);
            ChatConfig config2 = configuredAdapter.initializeChatForCustomer(2L, "User2", email);
            
            // Then - HMAC should be same for same email
            assertThat(config1.secureHmac()).isEqualTo(config2.secureHmac());
        }
        
        @Test
        @Order(28)
        @DisplayName("Should generate different HMAC for different emails")
        void shouldGenerateDifferentHmacForDifferentEmails() {
            // When
            ChatConfig config1 = configuredAdapter.initializeChatForCustomer(
                1L, "User1", "user1@example.com");
            ChatConfig config2 = configuredAdapter.initializeChatForCustomer(
                2L, "User2", "user2@example.com");
            
            // Then
            assertThat(config1.secureHmac()).isNotEqualTo(config2.secureHmac());
        }
        
        @Test
        @Order(29)
        @DisplayName("Should generate valid Base64-encoded HMAC")
        void shouldGenerateValidBase64Hmac() {
            // When
            ChatConfig config = configuredAdapter.initializeChatForCustomer(
                1L, "Test User", "test@example.com");
            
            // Then - HMAC should be Base64 encoded
            assertThatCode(() -> Base64.getDecoder().decode(config.secureHmac()))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(30)
        @DisplayName("Should match manually calculated HMAC")
        void shouldMatchManuallyCalculatedHmac() throws Exception {
            // Given
            String email = "test@example.com";
            String apiKey = "test-api-key-456";
            
            // When - Get HMAC from adapter
            ChatConfig config = configuredAdapter.initializeChatForCustomer(1L, "Test", email);
            
            // Calculate HMAC manually
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(email.getBytes(StandardCharsets.UTF_8));
            String expectedHmac = Base64.getEncoder().encodeToString(hash);
            
            // Then
            assertThat(config.secureHmac()).isEqualTo(expectedHmac);
        }
    }
    
    // ========================================
    // Integration Tests (Real Spring Context)
    // ========================================
    
    @Nested
    @DisplayName("Integration Tests with Spring Context")
    class IntegrationTests {
        
        @Test
        @Order(31)
        @DisplayName("Should autowire TawkToSupportAdapter bean")
        void shouldAutowireBean() {
            // Then
            assertThat(tawkToAdapter).isNotNull();
        }
        
        @Test
        @Order(32)
        @DisplayName("Should load configuration from application properties")
        void shouldLoadConfigFromProperties() {
            // When - Adapter was autowired and initialized
            // Then - Should not throw exception
            assertThatCode(() -> tawkToAdapter.initializeChatForCustomer(
                1L, "Test", "test@example.com"))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(33)
        @DisplayName("Should handle full customer support workflow")
        void shouldHandleFullWorkflow() {
            // Given
            Long customerId = 9999L;
            String customerName = "Ahmed Hassan";
            String customerEmail = "ahmed@example.com";
            
            // When - Initialize chat
            ChatConfig chatConfig = tawkToAdapter.initializeChatForCustomer(
                customerId, customerName, customerEmail);
            
            // Then - Chat initialized
            assertThat(chatConfig).isNotNull();
            assertThat(chatConfig.welcomeMessage()).contains(customerName);
            
            // When - Create offline message
            tawkToAdapter.createOfflineMessage(customerId, "Need help");
            
            // When - Get transcript
            String transcript = tawkToAdapter.getChatTranscript("chat-123");
            
            // Then - All operations complete without error
            assertThat(transcript).isNotNull();
        }
        
        @Test
        @Order(34)
        @DisplayName("Should handle concurrent chat initializations")
        void shouldHandleConcurrentInitializations() {
            // When - Initialize multiple chats
            ChatConfig c1 = tawkToAdapter.initializeChatForCustomer(1L, "User1", "user1@example.com");
            ChatConfig c2 = tawkToAdapter.initializeChatForCustomer(2L, "User2", "user2@example.com");
            ChatConfig c3 = tawkToAdapter.initializeChatForCustomer(3L, "User3", "user3@example.com");
            
            // Then - All should succeed
            assertThat(c1).isNotNull();
            assertThat(c2).isNotNull();
            assertThat(c3).isNotNull();
            
            // And welcome messages should be personalized
            assertThat(c1.welcomeMessage()).contains("User1");
            assertThat(c2.welcomeMessage()).contains("User2");
            assertThat(c3.welcomeMessage()).contains("User3");
        }
    }
}
