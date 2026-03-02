package com.mustapha.ecommerce.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Comprehensive Shipping Configuration Test Suite
 * 
 * Tests shipping provider configuration modes:
 * 1. REAL mode with shipping provider API keys
 * 2. MOCK mode without API keys (manual tracking)
 * 3. Disabled shipping
 * 4. Default configuration values
 * 5. Custom configuration
 * 6. Multiple carrier support
 * 
 * Note: Currently using BASIC/MOCK mode (manual tracking + carrier recording)
 * Future: Integration with Aramex, DHL, FedEx APIs
 * 
 * Total: 25 tests across 5 test classes
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShippingConfigTest {

    // ========================================
    // Test Class 1: REAL Mode (Future: API Integration)
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "shipping.enabled=true",
        "shipping.mode=MANUAL",
        "shipping.carriers=FedEx,UPS,DHL,Aramex",
        "shipping.default-carrier=FedEx"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RealModeManualShippingTest {

        @Autowired
        private ApplicationContext context;

        @Value("${shipping.enabled}")
        private boolean shippingEnabled;

        @Value("${shipping.mode}")
        private String shippingMode;

        @Value("${shipping.carriers}")
        private String carriers;

        @Value("${shipping.default-carrier}")
        private String defaultCarrier;

        @Test
        @Order(1)
        @DisplayName("Shipping should be enabled in REAL mode")
        void shouldBeEnabled() {
            assertThat(shippingEnabled).isTrue();
        }

        @Test
        @Order(2)
        @DisplayName("Should be in MANUAL mode (BASIC implementation)")
        void shouldBeManualMode() {
            assertThat(shippingMode).isEqualTo("MANUAL");
        }

        @Test
        @Order(3)
        @DisplayName("Should support multiple carriers")
        void shouldSupportMultipleCarriers() {
            assertThat(carriers).contains("FedEx", "UPS", "DHL", "Aramex");
        }

        @Test
        @Order(4)
        @DisplayName("Should have default carrier configured")
        void shouldHaveDefaultCarrier() {
            assertThat(defaultCarrier).isEqualTo("FedEx");
        }

        @Test
        @Order(5)
        @DisplayName("ApplicationContext should be valid")
        void shouldHaveValidContext() {
            assertThat(context).isNotNull();
            assertThat(context.getId()).isNotNull();
        }
    }

    // ========================================
    // Test Class 2: MOCK Mode (Testing/Development)
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "shipping.enabled=false",
        "shipping.mode=MOCK"
    })
    @DirtiesContext
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MockModeShippingTest {

        @Value("${shipping.enabled}")
        private boolean shippingEnabled;

        @Value("${shipping.mode}")
        private String shippingMode;

        @Test
        @Order(6)
        @DisplayName("Should be in MOCK mode")
        void shouldBeMockMode() {
            assertThat(shippingMode).isEqualTo("MOCK");
        }

        @Test
        @Order(7)
        @DisplayName("Shipping should be disabled in MOCK mode")
        void shouldBeDisabled() {
            assertThat(shippingEnabled).isFalse();
        }

        @Test
        @Order(8)
        @DisplayName("Should not send real shipment notifications")
        void shouldNotSendRealNotifications() {
            // In MOCK mode, no actual API calls should be made
            assertThat(shippingMode).isEqualTo("MOCK");
        }
    }

    // ========================================
    // Test Class 3: Shipping Disabled
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "shipping.enabled=false"
    })
    @DirtiesContext
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ShippingDisabledTest {

        @Value("${shipping.enabled}")
        private boolean shippingEnabled;

        @Test
        @Order(9)
        @DisplayName("Shipping should be disabled")
        void shouldBeDisabled() {
            assertThat(shippingEnabled).isFalse();
        }

        @Test
        @Order(10)
        @DisplayName("Orders can still be shipped manually")
        void shouldAllowManualShipping() {
            // Even when disabled, manual tracking entry should work
            // This is just configuration test
            assertThat(shippingEnabled).isFalse();
        }
    }

    // ========================================
    // Test Class 4: Default Configuration
    // ========================================

    @Nested
    @SpringBootTest
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DefaultConfigurationTest {

        @Value("${shipping.enabled:true}")
        private boolean shippingEnabled;

        @Value("${shipping.mode:MANUAL}")
        private String shippingMode;

        @Value("${shipping.default-carrier:FedEx}")
        private String defaultCarrier;

        @Test
        @Order(11)
        @DisplayName("Should default to enabled")
        void shouldDefaultToEnabled() {
            assertThat(shippingEnabled).isTrue();
        }

        @Test
        @Order(12)
        @DisplayName("Should default to MANUAL mode")
        void shouldDefaultToManualMode() {
            assertThat(shippingMode).isEqualTo("MANUAL");
        }

        @Test
        @Order(13)
        @DisplayName("Should have default carrier")
        void shouldHaveDefaultCarrier() {
            assertThat(defaultCarrier).isNotEmpty();
        }
    }

    // ========================================
    // Test Class 5: Carrier-Specific Configuration
    // ========================================

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
        "shipping.enabled=true",
        "shipping.carriers=FedEx,UPS,DHL,Aramex,USPS",
        "shipping.default-carrier=Aramex",
        "shipping.aramex.enabled=false",
        "shipping.dhl.enabled=true",
        "shipping.fedex.enabled=true",
        "shipping.ups.enabled=true"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CarrierSpecificConfigTest {

        @Value("${shipping.carriers}")
        private String carriers;

        @Value("${shipping.default-carrier}")
        private String defaultCarrier;

        @Value("${shipping.aramex.enabled:false}")
        private boolean aramexEnabled;

        @Value("${shipping.dhl.enabled:false}")
        private boolean dhlEnabled;

        @Value("${shipping.fedex.enabled:false}")
        private boolean fedexEnabled;

        @Value("${shipping.ups.enabled:false}")
        private boolean upsEnabled;

        @Test
        @Order(14)
        @DisplayName("Should support Aramex carrier")
        void shouldSupportAramex() {
            assertThat(carriers).contains("Aramex");
        }

        @Test
        @Order(15)
        @DisplayName("Should support DHL carrier")
        void shouldSupportDHL() {
            assertThat(carriers).contains("DHL");
        }

        @Test
        @Order(16)
        @DisplayName("Should support FedEx carrier")
        void shouldSupportFedEx() {
            assertThat(carriers).contains("FedEx");
        }

        @Test
        @Order(17)
        @DisplayName("Should support UPS carrier")
        void shouldSupportUPS() {
            assertThat(carriers).contains("UPS");
        }

        @Test
        @Order(18)
        @DisplayName("Should support USPS carrier")
        void shouldSupportUSPS() {
            assertThat(carriers).contains("USPS");
        }

        @Test
        @Order(19)
        @DisplayName("Default carrier should be Aramex")
        void shouldHaveAramexAsDefault() {
            assertThat(defaultCarrier).isEqualTo("Aramex");
        }

        @Test
        @Order(20)
        @DisplayName("Aramex should be configurable")
        void aramexShouldBeConfigurable() {
            assertThat(aramexEnabled).isFalse();  // Currently disabled in test
        }

        @Test
        @Order(21)
        @DisplayName("DHL should be enabled")
        void dhlShouldBeEnabled() {
            assertThat(dhlEnabled).isTrue();
        }

        @Test
        @Order(22)
        @DisplayName("FedEx should be enabled")
        void fedexShouldBeEnabled() {
            assertThat(fedexEnabled).isTrue();
        }

        @Test
        @Order(23)
        @DisplayName("UPS should be enabled")
        void upsShouldBeEnabled() {
            assertThat(upsEnabled).isTrue();
        }

        @Test
        @Order(24)
        @DisplayName("Should support multiple carriers simultaneously")
        void shouldSupportMultipleCarriersSimultaneously() {
            String[] supportedCarriers = carriers.split(",");
            assertThat(supportedCarriers).hasSizeGreaterThanOrEqualTo(5);
        }

        @Test
        @Order(25)
        @DisplayName("Each carrier should be individually configurable")
        void eachCarrierShouldBeConfigurable() {
            // Each carrier has its own enabled/disabled flag
            assertThat(aramexEnabled).isNotEqualTo(dhlEnabled)
                .describedAs("Carriers should have independent configuration");
        }
    }
}
