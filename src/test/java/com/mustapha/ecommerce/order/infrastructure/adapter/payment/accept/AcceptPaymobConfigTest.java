package com.mustapha.ecommerce.order.infrastructure.adapter.payment.accept;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test Suite: Accept (Paymob) Configuration
 * 
 * Tests:
 * 1. Configuration loading and validation
 * 2. isConfigured() validation logic
 * 3. Default values (base URL)
 * 4. Configuration validation
 * 5. Error handling for missing credentials
 * 6. Setters and getters
 * 7. Edge cases and special scenarios
 * 
 * Test Strategy:
 * - Create AcceptPaymobConfig instances manually
 * - Test various configuration scenarios
 * - Verify validation logic and edge cases
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AcceptPaymobConfigTest {

    // ========================================
    // Test Group 1: Configuration Loading
    // ========================================

    @Test
    @Order(1)
    @DisplayName("Should load configuration from direct instantiation")
    void testConfigurationLoading() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId("12345");
        
        // Then: Configuration should be loaded
        assertThat(testConfig).isNotNull();
        assertThat(testConfig.getBaseUrl()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Should use default base URL when not configured")
    void testDefaultBaseUrl() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        
        // Then: Should have default Accept production URL
        String baseUrl = testConfig.getBaseUrl();
        assertThat(baseUrl).isNotNull();
        assertThat(baseUrl).contains("accept.paymob.com");
    }

    // ========================================
    // Test Group 2: isConfigured() Validation
    // ========================================

    @Test
    @Order(20)
    @DisplayName("Should return false when not configured (using test config)")
    void testIsConfiguredInTestMode() {
        // Test environment typically doesn't have Accept credentials configured
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("");
        testConfig.setIntegrationId("");
        
        assertThat(testConfig.isConfigured()).isFalse();
    }

    @Test
    @Order(21)
    @DisplayName("Should return true when fully configured")
    void testIsConfiguredWhenFullyConfigured() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_api_key_xyz");
        testConfig.setIntegrationId("12345");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getApiKey()).isEqualTo("test_api_key_xyz");
        assertThat(testConfig.getIntegrationId()).isEqualTo("12345");
    }

    // ========================================
    // Test Group 3: API Key Validation
    // ========================================

    @Test
    @Order(22)
    @DisplayName("Should return false when API key is whitespace only")
    void testWhitespaceApiKey() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("  ");
        testConfig.setIntegrationId("12345");
        
        assertThat(testConfig.isConfigured()).isFalse();
    }

    @Test
    @Order(23)
    @DisplayName("Should return true when API key is valid")
    void testValidApiKey() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("valid_key");
        testConfig.setIntegrationId("12345");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getApiKey()).isEqualTo("valid_key");
    }

    // ========================================
    // Test Group 4: Integration ID Validation
    // ========================================

    @Test
    @Order(24)
    @DisplayName("Should return false when integration ID is whitespace only")
    void testWhitespaceIntegrationId() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("valid_key");
        testConfig.setIntegrationId("  ");
        
        assertThat(testConfig.isConfigured()).isFalse();
    }

    @Test
    @Order(25)
    @DisplayName("Should return true when integration ID is valid")
    void testValidIntegrationId() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("valid_key");
        testConfig.setIntegrationId("5560263");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getIntegrationId()).isEqualTo("5560263");
    }

    // ========================================
    // Test Group 5: Base URL Configuration
    // ========================================

    @Test
    @Order(26)
    @DisplayName("Should use configured base URL")
    void testConfiguredBaseUrl() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId("12345");
        testConfig.setBaseUrl("https://accept.paymob.com/api");
        
        assertThat(testConfig.getBaseUrl()).isEqualTo("https://accept.paymob.com/api");
    }

    @Test
    @Order(27)
    @DisplayName("Should support custom staging URL")
    void testStagingBaseUrl() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId("12345");
        testConfig.setBaseUrl("https://staging.accept.paymob.com/api");
        
        assertThat(testConfig.getBaseUrl()).isEqualTo("https://staging.accept.paymob.com/api");
    }

    @Test
    @Order(28)
    @DisplayName("Should use default base URL when not specified")
    void testDefaultBaseUrlWhenNotSpecified() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId("12345");
        
        assertThat(testConfig.getBaseUrl()).isEqualTo("https://accept.paymob.com/api");
    }

    // ========================================
    // Test Group 6: Configuration Validation
    // ========================================

    @Test
    @Order(29)
    @DisplayName("Should not throw when configuration is valid")
    void testValidateWithValidConfig() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId("12345");
        
        // Should not throw
        testConfig.validate();
    }

    @Test
    @Order(30)
    @DisplayName("Should throw IllegalStateException when configuration is invalid")
    void testValidateWithInvalidConfig() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("");
        testConfig.setIntegrationId("");
        
        assertThatThrownBy(() -> testConfig.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Accept configuration incomplete");
    }

    // ========================================
    // Test Group 7: Null Handling
    // ========================================

    @Test
    @Order(3)
    @DisplayName("Should handle null API key")
    void testNullApiKey() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey(null);
        testConfig.setIntegrationId("12345");
        
        assertThat(testConfig.isConfigured()).isFalse();
    }

    @Test
    @Order(4)
    @DisplayName("Should handle null integration ID")
    void testNullIntegrationId() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId(null);
        
        assertThat(testConfig.isConfigured()).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("Should handle both null credentials")
    void testBothNull() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey(null);
        testConfig.setIntegrationId(null);
        
        assertThat(testConfig.isConfigured()).isFalse();
    }

    // ========================================
    // Test Group 8: Setters and Getters
    // ========================================

    @Test
    @Order(6)
    @DisplayName("Should set and get API key")
    void testApiKeySetterGetter() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        String apiKey = "new_api_key_xyz";
        
        testConfig.setApiKey(apiKey);
        
        assertThat(testConfig.getApiKey()).isEqualTo(apiKey);
    }

    @Test
    @Order(7)
    @DisplayName("Should set and get integration ID")
    void testIntegrationIdSetterGetter() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        String integrationId = "9876543";
        
        testConfig.setIntegrationId(integrationId);
        
        assertThat(testConfig.getIntegrationId()).isEqualTo(integrationId);
    }

    @Test
    @Order(8)
    @DisplayName("Should set and get base URL")
    void testBaseUrlSetterGetter() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        String baseUrl = "https://test.accept.paymob.com/api";
        
        testConfig.setBaseUrl(baseUrl);
        
        assertThat(testConfig.getBaseUrl()).isEqualTo(baseUrl);
    }

    // ========================================
    // Test Group 9: Real-World Scenarios
    // ========================================

    @Test
    @Order(9)
    @DisplayName("Should accept real JWT token format")
    void testRealJWTToken() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5");
        testConfig.setIntegrationId("5560263");
        testConfig.setBaseUrl("https://accept.paymob.com/api");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getApiKey()).startsWith("ZXlK");
        assertThat(testConfig.getIntegrationId()).isEqualTo("5560263");
    }

    @Test
    @Order(10)
    @DisplayName("Should support local mock server URL")
    void testLocalMockUrl() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId("123");
        testConfig.setBaseUrl("http://localhost:8080/mock-accept");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getBaseUrl()).isEqualTo("http://localhost:8080/mock-accept");
    }

    // ========================================
    // Test Group 10: Edge Cases
    // ========================================

    @Test
    @Order(11)
    @DisplayName("Should handle very long API key")
    void testVeryLongApiKey() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        String longKey = "a".repeat(1000); // 1000 characters
        
        testConfig.setApiKey(longKey);
        testConfig.setIntegrationId("12345");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getApiKey()).hasSize(1000);
    }

    @Test
    @Order(12)
    @DisplayName("Should handle integration ID with leading zeros")
    void testIntegrationIdWithLeadingZeros() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        
        testConfig.setApiKey("test_key");
        testConfig.setIntegrationId("0001234");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getIntegrationId()).isEqualTo("0001234");
    }

    @Test
    @Order(13)
    @DisplayName("Should handle special characters in configuration")
    void testSpecialCharacters() {
        AcceptPaymobConfig testConfig = new AcceptPaymobConfig();
        
        testConfig.setApiKey("test_key_!@#$%^&*()");
        testConfig.setIntegrationId("12345");
        
        assertThat(testConfig.isConfigured()).isTrue();
        assertThat(testConfig.getApiKey()).contains("!@#$%^&*()");
    }
}
