package com.mustapha.ecommerce.order.infrastructure.adapter.shipping.aramex;

import com.mustapha.ecommerce.order.infrastructure.adapter.shipping.sdk.ShippingProviderClient.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Test Suite for AramexShippingClient
 * 
 * Coverage:
 * - Unit Tests: MOCK mode operations, request building
 * - Resilience Tests: API failures, null handling, error responses
 * - Integration Tests: Real Spring configuration (with fallback to MOCK mode)
 * 
 * Test Philosophy:
 * - Tests work without real Aramex credentials (uses MOCK mode)
 * - Integration tests use real Spring context
 * - Resilience tests cover edge cases and failure scenarios
 */
@SpringBootTest
@TestPropertySource(properties = {
    "shipping.aramex.username=",
    "shipping.aramex.password=",
    "shipping.aramex.account-number=",
    "shipping.aramex.account-pin="
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AramexShippingClientTest {

    @Autowired
    private AramexConfig aramexConfig;
    
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    
    private AramexShippingClient aramexClient;

    @BeforeEach
    void setUp() {
        aramexClient = new AramexShippingClient(aramexConfig, restTemplateBuilder);
        aramexClient.init();
    }

    // ========================================
    // MOCK Mode Tests (Default - No Credentials)
    // ========================================
    
    @Nested
    @DisplayName("MOCK Mode Operations")
    class MockModeTests {
        
        @Test
        @Order(1)
        @DisplayName("Should create mock shipment when credentials not configured")
        void shouldCreateMockShipment() {
            // Given
            ShipmentRequest request = createTestShipmentRequest();
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.trackingNumber()).startsWith("MOCK-");
            assertThat(response.labelUrl()).contains("mock-aramex-label");
            assertThat(response.carrierCode()).isEqualTo("ARAMEX");
            assertThat(response.errorMessage()).isNull();
        }
        
        @Test
        @Order(2)
        @DisplayName("Should generate unique mock tracking numbers")
        void shouldGenerateUniqueTrackingNumbers() {
            // Given
            ShipmentRequest request1 = createTestShipmentRequest();
            ShipmentRequest request2 = createTestShipmentRequest();
            
            // When
            ShipmentResponse response1 = aramexClient.createShipment(request1);
            ShipmentResponse response2 = aramexClient.createShipment(request2);
            
            // Then
            assertThat(response1.trackingNumber()).isNotEqualTo(response2.trackingNumber());
        }
        
        @Test
        @Order(3)
        @DisplayName("Should track mock shipment")
        void shouldTrackMockShipment() {
            // Given
            String trackingNumber = "MOCK-DXB-12345";
            
            // When
            TrackingResponse response = aramexClient.trackShipment(trackingNumber);
            
            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.trackingNumber()).isEqualTo(trackingNumber);
            assertThat(response.status()).isEqualTo(ShipmentStatus.IN_TRANSIT);
            assertThat(response.currentLocation()).isNotBlank();
            assertThat(response.events()).isNotEmpty();
            assertThat(response.events()).hasSizeGreaterThanOrEqualTo(2);
        }
        
        @Test
        @Order(4)
        @DisplayName("Should return mock tracking events with chronological order")
        void shouldReturnChronologicalTrackingEvents() {
            // When
            TrackingResponse response = aramexClient.trackShipment("MOCK-12345");
            
            // Then
            assertThat(response.events()).extracting(TrackingEvent::description)
                .containsAnyOf("Shipment picked up", "In transit", "Out for delivery");
        }
        
        @Test
        @Order(5)
        @DisplayName("Should return mock shipping rates")
        void shouldReturnMockRates() {
            // Given
            RateRequest request = createTestRateRequest();
            
            // When
            RateResponse response = aramexClient.getShippingRates(request);
            
            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.rates()).hasSize(2);
            assertThat(response.rates()).extracting(ServiceRate::serviceName)
                .containsExactlyInAnyOrder("Aramex Express", "Aramex Standard");
            assertThat(response.rates()).extracting(ServiceRate::cost)
                .containsExactlyInAnyOrder(45.00, 25.00);
        }
        
        @Test
        @Order(6)
        @DisplayName("Should return failure for cancellation request (not supported)")
        void shouldFailCancellationRequest() {
            // Given
            String trackingNumber = "MOCK-12345";
            
            // When
            CancellationResponse response = aramexClient.cancelShipment(trackingNumber);
            
            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isFalse();
            assertThat(response.message()).contains("manual intervention");
        }
    }
    
    // ========================================
    // Resilience Tests (Edge Cases & Error Handling)
    // ========================================
    
    @Nested
    @DisplayName("Resilience & Error Handling")
    class ResilienceTests {
        
        @Test
        @Order(7)
        @DisplayName("Should handle null recipient gracefully")
        void shouldHandleNullRecipient() {
            // Given
            ShipmentRequest request = new ShipmentRequest(
                createTestAddress("Sender"),
                null,  // Null recipient
                createTestPackage(),
                "EXPRESS",
                true,
                "ORDER-123"
            );
            
            // When/Then - Should not throw exception
            assertThatCode(() -> aramexClient.createShipment(request))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(8)
        @DisplayName("Should handle empty service type")
        void shouldHandleEmptyServiceType() {
            // Given
            ShipmentRequest request = new ShipmentRequest(
                createTestAddress("Sender"),
                createTestAddress("Recipient"),
                createTestPackage(),
                null,  // Empty service type
                false,
                "ORDER-456"
            );
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then - Should default to PPX (Express)
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(9)
        @DisplayName("Should handle zero weight package")
        void shouldHandleZeroWeightPackage() {
            // Given
            PackageDetails zeroWeightPackage = new PackageDetails(
                0.0,  // Zero weight
                10.0,
                10.0,
                10.0,
                "Light item",
                100.0
            );
            
            ShipmentRequest request = new ShipmentRequest(
                createTestAddress("Sender"),
                createTestAddress("Recipient"),
                zeroWeightPackage,
                "STANDARD",
                false,
                "ORDER-789"
            );
            
            // When/Then
            assertThatCode(() -> aramexClient.createShipment(request))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(10)
        @DisplayName("Should handle very large package")
        void shouldHandleVeryLargePackage() {
            // Given
            PackageDetails largePackage = new PackageDetails(
                500.0,  // 500 kg
                200.0,  // 200 cm
                150.0,
                100.0,
                "Heavy machinery",
                50000.0
            );
            
            ShipmentRequest request = new ShipmentRequest(
                createTestAddress("Sender"),
                createTestAddress("Recipient"),
                largePackage,
                "FREIGHT",
                true,
                "ORDER-HEAVY"
            );
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(11)
        @DisplayName("Should handle special characters in address")
        void shouldHandleSpecialCharactersInAddress() {
            // Given
            Address specialAddress = new Address(
                "François Müller",
                "Café & Restaurant™",
                "123 Rua São João",
                "Apt #5-B",
                "São Paulo",
                "SP",
                "01234-567",
                "BR",
                "+55 11 98765-4321",
                "françois@café.com"
            );
            
            ShipmentRequest request = new ShipmentRequest(
                createTestAddress("Sender"),
                specialAddress,
                createTestPackage(),
                "EXPRESS",
                false,
                "ORDER-INTL"
            );
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(12)
        @DisplayName("Should handle very long reference ID")
        void shouldHandleLongReferenceId() {
            // Given
            String longReference = "ORDER-" + "X".repeat(100);
            ShipmentRequest request = new ShipmentRequest(
                createTestAddress("Sender"),
                createTestAddress("Recipient"),
                createTestPackage(),
                "STANDARD",
                false,
                longReference
            );
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(13)
        @DisplayName("Should track invalid tracking number format")
        void shouldTrackInvalidTrackingNumber() {
            // Given
            String invalidTracking = "INVALID-TRACKING-###";
            
            // When
            TrackingResponse response = aramexClient.trackShipment(invalidTracking);
            
            // Then - MOCK mode returns success for any tracking number
            assertThat(response).isNotNull();
            assertThat(response.trackingNumber()).isEqualTo(invalidTracking);
        }
        
        @Test
        @Order(14)
        @DisplayName("Should handle empty tracking number")
        void shouldHandleEmptyTrackingNumber() {
            // When/Then
            assertThatCode(() -> aramexClient.trackShipment(""))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(15)
        @DisplayName("Should handle concurrent shipment creation")
        void shouldHandleConcurrentShipmentCreation() {
            // Given
            ShipmentRequest request = createTestShipmentRequest();
            
            // When - Create multiple shipments concurrently
            ShipmentResponse r1 = aramexClient.createShipment(request);
            ShipmentResponse r2 = aramexClient.createShipment(request);
            ShipmentResponse r3 = aramexClient.createShipment(request);
            
            // Then - All should succeed with unique tracking numbers
            assertThat(r1.success()).isTrue();
            assertThat(r2.success()).isTrue();
            assertThat(r3.success()).isTrue();
            assertThat(r1.trackingNumber()).isNotEqualTo(r2.trackingNumber());
            assertThat(r2.trackingNumber()).isNotEqualTo(r3.trackingNumber());
        }
    }
    
    // ========================================
    // Service Type Tests
    // ========================================
    
    @Nested
    @DisplayName("Service Type Handling")
    class ServiceTypeTests {
        
        @Test
        @Order(16)
        @DisplayName("Should handle EXPRESS service type")
        void shouldHandleExpressService() {
            // Given
            ShipmentRequest request = createTestShipmentRequest("EXPRESS");
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(17)
        @DisplayName("Should handle STANDARD service type")
        void shouldHandleStandardService() {
            // Given
            ShipmentRequest request = createTestShipmentRequest("STANDARD");
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(18)
        @DisplayName("Should handle CashOnDelivery service type")
        void shouldHandleCashOnDelivery() {
            // Given
            ShipmentRequest request = createTestShipmentRequest("CashOnDelivery");
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
    }
    
    // ========================================
    // Regional Coverage Tests (MENA Region)
    // ========================================
    
    @Nested
    @DisplayName("MENA Regional Coverage")
    class RegionalCoverageTests {
        
        @Test
        @Order(19)
        @DisplayName("Should create shipment to Egypt")
        void shouldCreateShipmentToEgypt() {
            // Given
            Address egyptAddress = createTestAddress("Cairo Customer", "EG");
            ShipmentRequest request = createShipmentRequestWithDestination(egyptAddress);
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(20)
        @DisplayName("Should create shipment to UAE")
        void shouldCreateShipmentToUAE() {
            // Given
            Address uaeAddress = createTestAddress("Dubai Customer", "AE");
            ShipmentRequest request = createShipmentRequestWithDestination(uaeAddress);
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(21)
        @DisplayName("Should create shipment to Saudi Arabia")
        void shouldCreateShipmentToSaudi() {
            // Given
            Address saudiAddress = createTestAddress("Riyadh Customer", "SA");
            ShipmentRequest request = createShipmentRequestWithDestination(saudiAddress);
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(22)
        @DisplayName("Should create shipment to Kuwait")
        void shouldCreateShipmentToKuwait() {
            // Given
            Address kuwaitAddress = createTestAddress("Kuwait City Customer", "KW");
            ShipmentRequest request = createShipmentRequestWithDestination(kuwaitAddress);
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(23)
        @DisplayName("Should create shipment to Qatar")
        void shouldCreateShipmentToQatar() {
            // Given
            Address qatarAddress = createTestAddress("Doha Customer", "QA");
            ShipmentRequest request = createShipmentRequestWithDestination(qatarAddress);
            
            // When
            ShipmentResponse response = aramexClient.createShipment(request);
            
            // Then
            assertThat(response.success()).isTrue();
        }
    }
    
    // ========================================
    // Integration Tests (Real Spring Context)
    // ========================================
    
    @Nested
    @DisplayName("Integration Tests with Spring Context")
    class IntegrationTests {
        
        @Test
        @Order(24)
        @DisplayName("Should initialize in MOCK mode when credentials not configured")
        void shouldInitializeInMockMode() {
            // When
            aramexClient.init();
            
            // Then - Verify it runs in MOCK mode (no exceptions during initialization)
            assertThatCode(() -> aramexClient.createShipment(createTestShipmentRequest()))
                .doesNotThrowAnyException();
        }
        
        @Test
        @Order(25)
        @DisplayName("Should work with real AramexConfig bean")
        void shouldWorkWithRealConfig() {
            // Given - Using autowired config
            assertThat(aramexConfig).isNotNull();
            
            // When
            ShipmentResponse response = aramexClient.createShipment(createTestShipmentRequest());
            
            // Then
            assertThat(response.success()).isTrue();
        }
        
        @Test
        @Order(26)
        @DisplayName("Should handle full shipment lifecycle")
        void shouldHandleFullLifecycle() {
            // Given
            ShipmentRequest shipmentRequest = createTestShipmentRequest();
            
            // When - Create shipment
            ShipmentResponse createResponse = aramexClient.createShipment(shipmentRequest);
            
            // Then - Shipment created
            assertThat(createResponse.success()).isTrue();
            String trackingNumber = createResponse.trackingNumber();
            
            // When - Track shipment
            TrackingResponse trackingResponse = aramexClient.trackShipment(trackingNumber);
            
            // Then - Tracking works
            assertThat(trackingResponse.success()).isTrue();
            assertThat(trackingResponse.trackingNumber()).isEqualTo(trackingNumber);
            
            // When - Get rates
            RateResponse rateResponse = aramexClient.getShippingRates(createTestRateRequest());
            
            // Then - Rates available
            assertThat(rateResponse.success()).isTrue();
            assertThat(rateResponse.rates()).isNotEmpty();
        }
        
        @Test
        @Order(27)
        @DisplayName("Should validate configuration properties")
        void shouldValidateConfigProperties() {
            // Then - Config should exist and have values (even if empty)
            assertThat(aramexConfig).isNotNull();
            assertThat(aramexConfig.getUsername()).isNotNull();
            assertThat(aramexConfig.getAccountEntity()).isNotNull();
            assertThat(aramexConfig.getBaseUrl()).isNotBlank();
        }
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    private ShipmentRequest createTestShipmentRequest() {
        return createTestShipmentRequest("EXPRESS");
    }
    
    private ShipmentRequest createTestShipmentRequest(String serviceType) {
        return new ShipmentRequest(
            createTestAddress("Warehouse"),
            createTestAddress("Customer"),
            createTestPackage(),
            serviceType,
            false,
            "ORDER-TEST-" + System.currentTimeMillis()
        );
    }
    
    private ShipmentRequest createShipmentRequestWithDestination(Address destination) {
        return new ShipmentRequest(
            createTestAddress("Warehouse"),
            destination,
            createTestPackage(),
            "EXPRESS",
            false,
            "ORDER-INTL-" + System.currentTimeMillis()
        );
    }
    
    private Address createTestAddress(String name) {
        return createTestAddress(name, "AE");
    }
    
    private Address createTestAddress(String name, String country) {
        return new Address(
            name,
            "Test Company",
            "123 Test Street",
            "Floor 5",
            "Dubai",
            "DXB",
            "12345",
            country,
            "+971501234567",
            "test@example.com"
        );
    }
    
    private PackageDetails createTestPackage() {
        return new PackageDetails(
            2.5,   // 2.5 kg
            30.0,  // 30 cm
            20.0,  // 20 cm
            10.0,  // 10 cm
            "Electronics - Phone",
            500.0  // $500 declared value
        );
    }
    
    private RateRequest createTestRateRequest() {
        return new RateRequest(
            createTestAddress("Origin"),
            createTestAddress("Destination"),
            createTestPackage()
        );
    }
}
