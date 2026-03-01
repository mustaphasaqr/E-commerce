package com.mustapha.ecommerce.shared.config;

import com.sendgrid.SendGrid;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Suite: EmailConfig Configuration Logic
 * 
 * Tests cover:
 * 1. SendGrid bean creation
 * 2. MOCK vs REAL mode detection
 * 3. Configuration properties
 * 4. Default values
 * 5. Email enabled/disabled states
 * 
 * Total: 20 tests across 5 test classes
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailConfigTest {

    // ========================================
    // Test Class 1: REAL Mode (API Key Provided)
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "email.sendgrid.api-key=SG.test_api_key_12345678901234567890",
        "email.sendgrid.from-email=test@example.com",
        "email.sendgrid.from-name=Test App",
        "email.enabled=true"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RealModeWithApiKeyTest {

        @Autowired
        private ApplicationContext context;

        @Autowired
        private EmailConfig emailConfig;

        @Value("${email.sendgrid.from-email}")
        private String fromEmail;

        @Value("${email.sendgrid.from-name}")
        private String fromName;

        @Test
        @Order(1)
        @DisplayName("Should create SendGrid bean in REAL mode")
        void testSendGridBeanCreated() {
            SendGrid sendGrid = context.getBean(SendGrid.class);
            assertThat(sendGrid).isNotNull();
        }

        @Test
        @Order(2)
        @DisplayName("Should inject from-email property")
        void testFromEmailProperty() {
            assertThat(fromEmail).isEqualTo("test@example.com");
        }

        @Test
        @Order(3)
        @DisplayName("Should inject from-name property")
        void testFromNameProperty() {
            assertThat(fromName).isEqualTo("Test App");
        }

        @Test
        @Order(4)
        @DisplayName("EmailConfig getters should return correct values")
        void testEmailConfigGetters() {
            assertThat(emailConfig.getFromEmail()).isEqualTo("test@example.com");
            assertThat(emailConfig.getFromName()).isEqualTo("Test App");
        }
    }

    // ========================================
    // Test Class 2: MOCK Mode (No API Key)
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "email.sendgrid.from-email=noreply@example.com",
        "email.sendgrid.from-name=Mock App",
        "email.enabled=true"
    })
    @DirtiesContext
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MockModeWithoutApiKeyTest {

        @Autowired
        private ApplicationContext context;

        @Autowired
        private EmailConfig emailConfig;

        @Autowired(required = false)
        private SendGrid sendGrid;

        @Value("${email.sendgrid.from-email}")
        private String fromEmail;

        @Test
        @Order(5)
        @DisplayName("Should return null SendGrid bean in MOCK mode")
        void testNullSendGridBean() {
            // SendGrid bean should be null in MOCK mode (no API key)
            assertThat(sendGrid).isNull();
        }

        @Test
        @Order(6)
        @DisplayName("Should inject from-email in MOCK mode")
        void testFromEmailInMockMode() {
            assertThat(fromEmail).isEqualTo("noreply@example.com");
        }

        @Test
        @Order(7)
        @DisplayName("EmailConfig should exist in MOCK mode")
        void testEmailConfigExists() {
            assertThat(emailConfig).isNotNull();
            assertThat(emailConfig.getFromEmail()).isEqualTo("noreply@example.com");
        }

        @Test
        @Order(8)
        @DisplayName("Getters should work in MOCK mode")
        void testGettersInMockMode() {
            assertThat(emailConfig.getFromEmail()).isNotBlank();
            assertThat(emailConfig.getFromName()).isNotBlank();
        }
    }

    // ========================================
    // Test Class 3: Email Disabled
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "email.sendgrid.api-key=SG.disabled_test_key",
        "email.sendgrid.from-email=disabled@example.com",
        "email.enabled=false"
    })
    @DirtiesContext
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EmailDisabledTest {

        @Autowired
        private ApplicationContext context;

        @Autowired
        private EmailConfig emailConfig;

        @Autowired(required = false)
        private SendGrid sendGrid;

        @Value("${email.enabled}")
        private boolean emailEnabled;

        @Test
        @Order(9)
        @DisplayName("Should return null when email is disabled")
        void testSendGridNullWhenDisabled() {
            // SendGrid bean should be null when email is disabled
            assertThat(sendGrid).isNull();
        }

        @Test
        @Order(10)
        @DisplayName("Should have email.enabled=false")
        void testEmailDisabledFlag() {
            assertThat(emailEnabled).isFalse();
        }

        @Test
        @Order(11)
        @DisplayName("EmailConfig should exist when disabled")
        void testEmailConfigExists() {
            assertThat(emailConfig).isNotNull();
        }

        @Test
        @Order(12)
        @DisplayName("Getters should work when disabled")
        void testGettersWhenDisabled() {
            assertThat(emailConfig.getFromEmail()).isEqualTo("disabled@example.com");
        }
    }

    // ========================================
    // Test Class 4: Default Configuration
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "email.sendgrid.api-key=SG.default_test"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DefaultConfigurationTest {

        @Autowired
        private ApplicationContext context;

        @Autowired
        private EmailConfig emailConfig;

        @Value("${email.sendgrid.from-email:noreply@ecommerce.com}")
        private String fromEmail;

        @Value("${email.sendgrid.from-name:E-Commerce Platform}")
        private String fromName;

        @Value("${email.enabled:true}")
        private boolean emailEnabled;

        @Test
        @Order(13)
        @DisplayName("Should use default from-email")
        void testDefaultFromEmail() {
            assertThat(fromEmail).isEqualTo("noreply@ecommerce.com");
        }

        @Test
        @Order(14)
        @DisplayName("Should use default from-name")
        void testDefaultFromName() {
            assertThat(fromName).isEqualTo("E-Commerce Platform");
        }

        @Test
        @Order(15)
        @DisplayName("Should default email.enabled to true")
        void testDefaultEmailEnabled() {
            assertThat(emailEnabled).isTrue();
        }

        @Test
        @Order(16)
        @DisplayName("Should create SendGrid with defaults")
        void testSendGridWithDefaults() {
            SendGrid sendGrid = context.getBean(SendGrid.class);
            assertThat(sendGrid).isNotNull();
        }

        @Test
        @Order(17)
        @DisplayName("EmailConfig should return defaults")
        void testEmailConfigDefaults() {
            assertThat(emailConfig.getFromEmail()).isEqualTo("noreply@ecommerce.com");
            assertThat(emailConfig.getFromName()).isEqualTo("E-Commerce Platform");
        }
    }

    // ========================================
    // Test Class 5: Custom Configuration
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "email.sendgrid.api-key=SG.custom_test_key",
        "email.sendgrid.from-email=custom@mycompany.com",
        "email.sendgrid.from-name=My Company Support",
        "email.enabled=true"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CustomConfigurationTest {

        @Autowired
        private EmailConfig emailConfig;

        @Value("${email.sendgrid.from-email}")
        private String fromEmail;

        @Value("${email.sendgrid.from-name}")
        private String fromName;

        @Test
        @Order(18)
        @DisplayName("Should use custom from-email")
        void testCustomFromEmail() {
            assertThat(fromEmail).isEqualTo("custom@mycompany.com");
            assertThat(emailConfig.getFromEmail()).isEqualTo("custom@mycompany.com");
        }

        @Test
        @Order(19)
        @DisplayName("Should use custom from-name")
        void testCustomFromName() {
            assertThat(fromName).isEqualTo("My Company Support");
            assertThat(emailConfig.getFromName()).isEqualTo("My Company Support");
        }

        @Test
        @Order(20)
        @DisplayName("EmailConfig getters should return custom values")
        void testEmailConfigCustomGetters() {
            assertThat(emailConfig.getFromEmail()).isEqualTo("custom@mycompany.com");
            assertThat(emailConfig.getFromName()).isEqualTo("My Company Support");
        }
    }
}

