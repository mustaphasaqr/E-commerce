package com.mustapha.ecommerce.order.infrastructure.adapter.shipping.aramex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mustapha.ecommerce.order.infrastructure.adapter.shipping.sdk.ShippingProviderClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Aramex Shipping HTTP Client
 * Responsibility: Make HTTP requests to Aramex Shipping API
 * 
 * Aramex API Documentation:
 * https://www.aramex.com/docs/default-source/resourses/shipping-services-api-manual.pdf
 * 
 * Shipping Flow:
 * 1. POST /ShippingAPI.V2/Shipping/Service_1_0.svc/json/CreateShipments → Create shipment & get tracking number
 * 2. POST /ShippingAPI.V2/Shipping/Service_1_0.svc/json/TrackShipments → Track shipment status
 * 3. POST /ShippingAPI.V2/Shipping/Service_1_0.svc/json/GetRate → Get shipping rates (optional)
 * 
 * Features:
 * - Graceful degradation (MANUAL mode if not configured)
 * - Thread-safe shipment tracking
 * - Error handling with fallback to manual entry
 * - Support for Egypt, UAE, Saudi Arabia, and MENA region
 * 
 * MOCK Mode:
 * - When credentials not configured
 * - Returns simulated tracking numbers (MOCK-...)
 * - Logs all operations
 * - Application works without Aramex account
 */
@Component
public class AramexShippingClient implements ShippingProviderClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AramexShippingClient.class);
    
    private final AramexConfig config;
    private final RestTemplate restTemplate;
    
    private boolean isRealModeEnabled = false;
    
    // Mock tracking number counter (for test mode)
    private int mockTrackingCounter = 1000;
    
    public AramexShippingClient(AramexConfig config, RestTemplateBuilder builder) {
        this.config = config;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofSeconds(45))
                .build();
    }
    
    @PostConstruct
    public void init() {
        isRealModeEnabled = config.isConfigured();
        
        if (isRealModeEnabled) {
            logger.info("✅ AramexShippingClient initialized (REAL mode)");
            logger.info("   Account: {}", config.getAccountNumber());
            logger.info("   Entity: {}", config.getAccountEntity());
        } else {
            logger.warn("⚠️ AramexShippingClient initialized in MANUAL/MOCK mode");
            logger.warn("   Shipments will use manual tracking number entry");
        }
    }
    
    /**
     * Create shipment with Aramex
     * Returns tracking number and label URL
     */
    @Override
    public ShipmentResponse createShipment(ShipmentRequest request) {
        if (!isRealModeEnabled) {
            return createMockShipment(request);
        }
        
        try {
            // Build Aramex shipment request
            AramexCreateShipmentRequest aramexRequest = buildAramexShipmentRequest(request);
            
            String url = config.getBaseUrl() + "/Shipping/Service_1_0.svc/json/CreateShipments";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AramexCreateShipmentRequest> entity = new HttpEntity<>(aramexRequest, headers);
            
            logger.info("📦 Creating Aramex shipment for order: {}", request.reference());
            
            ResponseEntity<AramexShipmentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AramexShipmentResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseShipmentResponse(response.getBody(), request);
            } else {
                logger.error("❌ Aramex shipment creation failed: HTTP {}", response.getStatusCode());
                return new ShipmentResponse(
                    false, null, null, null, null,
                    "HTTP error: " + response.getStatusCode()
                );
            }
            
        } catch (RestClientException e) {
            logger.error("❌ Aramex API error: {}", e.getMessage());
            return new ShipmentResponse(
                false, null, null, null, null,
                "API error: " + e.getMessage()
            );
        }
    }
    
    /**
     * Track shipment with Aramex
     */
    @Override
    public TrackingResponse trackShipment(String trackingNumber) {
        if (!isRealModeEnabled) {
            return createMockTrackingResponse(trackingNumber);
        }
        
        try {
            AramexTrackingRequest aramexRequest = new AramexTrackingRequest(
                buildClientInfo(),
                new AramexTransaction("", "TrackShipments"),
                List.of(trackingNumber)
            );
            
            String url = config.getBaseUrl() + "/Shipping/Service_1_0.svc/json/TrackShipments";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AramexTrackingRequest> entity = new HttpEntity<>(aramexRequest, headers);
            
            logger.info("🔍 Tracking Aramex shipment: {}", trackingNumber);
            
            ResponseEntity<AramexTrackingResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AramexTrackingResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTrackingResponse(response.getBody(), trackingNumber);
            } else {
                logger.error("❌ Aramex tracking failed: HTTP {}", response.getStatusCode());
                return new TrackingResponse(
                    false, trackingNumber, null, null, null, List.of(),
                    "HTTP error: " + response.getStatusCode()
                );
            }
            
        } catch (RestClientException e) {
            logger.error("❌ Aramex tracking error: {}", e.getMessage());
            return new TrackingResponse(
                false, trackingNumber, null, null, null, List.of(),
                "API error: " + e.getMessage()
            );
        }
    }
    
    /**
     * Get shipping rates from Aramex
     */
    @Override
    public RateResponse getShippingRates(RateRequest request) {
        if (!isRealModeEnabled) {
            return createMockRateResponse();
        }
        
        try {
            AramexRateRequest aramexRequest = buildAramexRateRequest(request);
            
            String url = config.getBaseUrl() + "/Shipping/Service_1_0.svc/json/CalculateRate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AramexRateRequest> entity = new HttpEntity<>(aramexRequest, headers);
            
            logger.info("💰 Getting Aramex shipping rates");
            
            ResponseEntity<AramexRateResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AramexRateResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseRateResponse(response.getBody());
            } else {
                logger.error("❌ Aramex rate calculation failed: HTTP {}", response.getStatusCode());
                return new RateResponse(
                    false, List.of(),
                    "HTTP error: " + response.getStatusCode()
                );
            }
            
        } catch (RestClientException e) {
            logger.error("❌ Aramex rate calculation error: {}", e.getMessage());
            return new RateResponse(
                false, List.of(),
                "API error: " + e.getMessage()
            );
        }
    }
    
    /**
     * Cancel shipment (not widely supported by Aramex API)
     */
    @Override
    public CancellationResponse cancelShipment(String trackingNumber) {
        logger.warn("⚠️ Aramex shipment cancellation requires contacting customer service");
        return new CancellationResponse(
            false,
            "Aramex cancellation requires manual intervention - contact customer service"
        );
    }
    
    // ========================================
    // Helper Methods - Request Building
    // ========================================
    
    private AramexCreateShipmentRequest buildAramexShipmentRequest(ShipmentRequest request) {
        AramexClientInfo clientInfo = buildClientInfo();
        AramexTransaction transaction = new AramexTransaction("", "CreateShipments");
        
        // Determine sender address (use default from config or provided in request)
        AramexAddress sender = buildSenderAddress(request.sender());
        AramexAddress recipient = buildRecipientAddress(request.recipient());
        
        AramexShipmentDetails details = new AramexShipmentDetails(
            sender,
            recipient,
            List.of(buildPackage(request.packageDetails())),
            request.serviceType() != null ? request.serviceType() : "PPX", // PPX = Express
            "CashOnDelivery".equalsIgnoreCase(request.serviceType()) ? 0.0 : null,
            request.reference(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        
        return new AramexCreateShipmentRequest(
            clientInfo,
            transaction,
            List.of(details),
            null  // Label info (optional)
        );
    }
    
    private AramexClientInfo buildClientInfo() {
        return new AramexClientInfo(
            config.getUsername(),
            config.getPassword(),
            "1.0",
            config.getAccountNumber(),
            config.getAccountPin(),
            config.getAccountEntity(),
            "EGP"  // Default currency (can be made configurable)
        );
    }
    
    private AramexAddress buildSenderAddress(Address sender) {
        // Use default sender from config if not provided
        if (config.hasSenderAddress() && (sender == null || sender.name() == null)) {
            return new AramexAddress(
                config.getSenderName(),
                config.getSenderCompany(),
                config.getSenderAddress1(),
                config.getSenderAddress2(),
                config.getSenderCity(),
                config.getSenderState(),
                config.getSenderPostalCode(),
                config.getSenderCountry(),
                config.getSenderPhone(),
                config.getSenderEmail()
            );
        }
        
        return new AramexAddress(
            sender.name(),
            sender.company(),
            sender.address1(),
            sender.address2(),
            sender.city(),
            sender.state(),
            sender.postalCode(),
            sender.country(),
            sender.phone(),
            sender.email()
        );
    }
    
    private AramexAddress buildRecipientAddress(Address recipient) {
        return new AramexAddress(
            recipient.name(),
            recipient.company(),
            recipient.address1(),
            recipient.address2(),
            recipient.city(),
            recipient.state(),
            recipient.postalCode(),
            recipient.country(),
            recipient.phone(),
            recipient.email()
        );
    }
    
    private AramexPackage buildPackage(PackageDetails details) {
        return new AramexPackage(
            details.weight(),
            details.length(),
            details.width(),
            details.height(),
            details.description(),
            1  // Quantity (always 1 for now)
        );
    }
    
    private AramexRateRequest buildAramexRateRequest(RateRequest request) {
        return new AramexRateRequest(
            buildClientInfo(),
            new AramexTransaction("", "CalculateRate"),
            buildRecipientAddress(request.destination()),
            buildSenderAddress(request.origin()),
            buildPackage(request.packageDetails()),
            "PPX"  // Default to Express service
        );
    }
    
    // ========================================
    // Helper Methods - Response Parsing
    // ========================================
    
    private ShipmentResponse parseShipmentResponse(AramexShipmentResponse aramexResponse, ShipmentRequest originalRequest) {
        if (aramexResponse.hasErrors()) {
            logger.error("❌ Aramex shipment error: {}", aramexResponse.errorMessage());
            return new ShipmentResponse(
                false, null, null, null, null,
                aramexResponse.errorMessage()
            );
        }
        
        if (aramexResponse.shipments() != null && !aramexResponse.shipments().isEmpty()) {
            AramexShipmentResult result = aramexResponse.shipments().get(0);
            
            logger.info("✅ Aramex shipment created: {}", result.trackingNumber());
            
            return new ShipmentResponse(
                true,
                result.trackingNumber(),
                result.labelUrl(),
                "ARAMEX",
                null,  // Aramex doesn't always return estimated delivery
                null
            );
        }
        
        return new ShipmentResponse(
            false, null, null, null, null,
            "No shipment data returned from Aramex"
        );
    }
    
    private TrackingResponse parseTrackingResponse(AramexTrackingResponse aramexResponse, String trackingNumber) {
        if (aramexResponse.hasErrors()) {
            return new TrackingResponse(
                false, trackingNumber, null, null, null, List.of(),
                aramexResponse.errorMessage()
            );
        }
        
        if (aramexResponse.trackingResults() != null && !aramexResponse.trackingResults().isEmpty()) {
            AramexTrackingResult result = aramexResponse.trackingResults().get(0);
            
            ShipmentStatus status = mapAramexStatus(result.updateDescription());
            
            List<TrackingEvent> events = result.updateCode() != null ? 
                List.of(new TrackingEvent(
                    result.updateDateTime(),
                    result.updateLocation(),
                    result.updateCode(),
                    result.updateDescription()
                )) : List.of();
            
            return new TrackingResponse(
                true,
                trackingNumber,
                status,
                result.updateLocation(),
                null,
                events,
                null
            );
        }
        
        return new TrackingResponse(
            false, trackingNumber, null, null, null, List.of(),
            "No tracking data found for: " + trackingNumber
        );
    }
    
    private RateResponse parseRateResponse(AramexRateResponse aramexResponse) {
        if (aramexResponse.hasErrors()) {
            return new RateResponse(
                false, List.of(),
                aramexResponse.errorMessage()
            );
        }
        
        if (aramexResponse.totalAmount() != null) {
            ServiceRate rate = new ServiceRate(
                "EXPRESS",
                "Aramex Express",
                aramexResponse.totalAmount(),
                aramexResponse.currency() != null ? aramexResponse.currency() : "USD",
                null
            );
            
            return new RateResponse(true, List.of(rate), null);
        }
        
        return new RateResponse(
            false, List.of(),
            "No rate information returned"
        );
    }
    
    private ShipmentStatus mapAramexStatus(String description) {
        if (description == null) return ShipmentStatus.IN_TRANSIT;
        
        String lower = description.toLowerCase();
        if (lower.contains("delivered")) return ShipmentStatus.DELIVERED;
        if (lower.contains("out for delivery")) return ShipmentStatus.OUT_FOR_DELIVERY;
        if (lower.contains("picked")) return ShipmentStatus.PICKED_UP;
        if (lower.contains("exception") || lower.contains("failed")) return ShipmentStatus.EXCEPTION;
        if (lower.contains("cancelled")) return ShipmentStatus.CANCELLED;
        if (lower.contains("returned")) return ShipmentStatus.RETURNED;
        
        return ShipmentStatus.IN_TRANSIT;
    }
    
    // ========================================
    // MOCK Mode Methods
    // ========================================
    
    private ShipmentResponse createMockShipment(ShipmentRequest request) {
        String mockTracking = "MOCK-" + config.getAccountEntity() + "-" + (mockTrackingCounter++);
        
        logger.info("📦 [MOCK] Created shipment for order: {}", request.reference());
        logger.info("   Tracking: {}", mockTracking);
        
        // Handle null recipient gracefully
        if (request.recipient() != null) {
            logger.info("   Recipient: {}, {}", request.recipient().city(), request.recipient().country());
        } else {
            logger.info("   Recipient: null (invalid request)");
        }
        
        return new ShipmentResponse(
            true,
            mockTracking,
            "https://mock-aramex-label.com/" + mockTracking,
            "ARAMEX",
            LocalDateTime.now().plusDays(3).toString(),
            null
        );
    }
    
    private TrackingResponse createMockTrackingResponse(String trackingNumber) {
        logger.info("🔍 [MOCK] Tracking shipment: {}", trackingNumber);
        
        return new TrackingResponse(
            true,
            trackingNumber,
            ShipmentStatus.IN_TRANSIT,
            "Distribution Center - Dubai",
            LocalDateTime.now().plusDays(2).toString(),
            List.of(
                new TrackingEvent(
                    LocalDateTime.now().minusDays(1).toString(),
                    "Warehouse - Cairo",
                    "SH010",
                    "Shipment picked up"
                ),
                new TrackingEvent(
                    LocalDateTime.now().toString(),
                    "Distribution Center - Dubai",
                    "SH014",
                    "In transit"
                )
            ),
            null
        );
    }
    
    private RateResponse createMockRateResponse() {
        logger.info("💰 [MOCK] Calculating shipping rates");
        
        return new RateResponse(
            true,
            List.of(
                new ServiceRate("EXPRESS", "Aramex Express", 45.00, "USD", null),
                new ServiceRate("STANDARD", "Aramex Standard", 25.00, "USD", null)
            ),
            null
        );
    }
    
    // ========================================
    // Aramex API DTOs (Request/Response)
    // ========================================
    
    record AramexClientInfo(
        @JsonProperty("UserName") String userName,
        @JsonProperty("Password") String password,
        @JsonProperty("Version") String version,
        @JsonProperty("AccountNumber") String accountNumber,
        @JsonProperty("AccountPin") String accountPin,
        @JsonProperty("AccountEntity") String accountEntity,
        @JsonProperty("AccountCountryCode") String accountCountryCode
    ) {}
    
    record AramexTransaction(
        @JsonProperty("Reference1") String reference1,
        @JsonProperty("Reference2") String reference2
    ) {}
    
    record AramexAddress(
        @JsonProperty("Name") String name,
        @JsonProperty("Company") String company,
        @JsonProperty("Line1") String line1,
        @JsonProperty("Line2") String line2,
        @JsonProperty("City") String city,
        @JsonProperty("StateOrProvinceCode") String stateOrProvinceCode,
        @JsonProperty("PostCode") String postCode,
        @JsonProperty("CountryCode") String countryCode,
        @JsonProperty("CellPhone") String cellPhone,
        @JsonProperty("Email") String email
    ) {}
    
    record AramexPackage(
        @JsonProperty("ActualWeight") double actualWeight,
        @JsonProperty("Length") double length,
        @JsonProperty("Width") double width,
        @JsonProperty("Height") double height,
        @JsonProperty("Description") String description,
        @JsonProperty("Quantity") int quantity
    ) {}
    
    record AramexShipmentDetails(
        @JsonProperty("Shipper") AramexAddress shipper,
        @JsonProperty("Consignee") AramexAddress consignee,
        @JsonProperty("Details") List<AramexPackage> details,
        @JsonProperty("ProductGroup") String productGroup,
        @JsonProperty("CashOnDeliveryAmount") Double cashOnDeliveryAmount,
        @JsonProperty("Reference") String reference,
        @JsonProperty("ShipmentDate") String shipmentDate
    ) {}
    
    record AramexCreateShipmentRequest(
        @JsonProperty("ClientInfo") AramexClientInfo clientInfo,
        @JsonProperty("Transaction") AramexTransaction transaction,
        @JsonProperty("Shipments") List<AramexShipmentDetails> shipments,
        @JsonProperty("LabelInfo") Object labelInfo
    ) {}
    
    record AramexShipmentResult(
        @JsonProperty("ID") String id,
        @JsonProperty("GuidID") String guidId,
        @JsonProperty("Reference1") String reference1,
        @JsonProperty("Reference2") String reference2,
        @JsonProperty("ForeignHAWB") String trackingNumber,
        @JsonProperty("ShipmentLabel") AramexLabel shipmentLabel
    ) {
        String labelUrl() {
            return shipmentLabel != null ? shipmentLabel.labelURL() : null;
        }
    }
    
    record AramexLabel(
        @JsonProperty("LabelURL") String labelURL
    ) {}
    
    record AramexShipmentResponse(
        @JsonProperty("Shipments") List<AramexShipmentResult> shipments,
        @JsonProperty("HasErrors") boolean hasErrors,
        @JsonProperty("Notifications") List<AramexNotification> notifications
    ) {
        String errorMessage() {
            if (notifications != null && !notifications.isEmpty()) {
                return notifications.get(0).message();
            }
            return "Unknown error";
        }
    }
    
    record AramexNotification(
        @JsonProperty("Code") String code,
        @JsonProperty("Message") String message
    ) {}
    
    record AramexTrackingRequest(
        @JsonProperty("ClientInfo") AramexClientInfo clientInfo,
        @JsonProperty("Transaction") AramexTransaction transaction,
        @JsonProperty("Shipments") List<String> shipments
    ) {}
    
    record AramexTrackingResult(
        @JsonProperty("WaybillNumber") String waybillNumber,
        @JsonProperty("UpdateCode") String updateCode,
        @JsonProperty("UpdateDescription") String updateDescription,
        @JsonProperty("UpdateDateTime") String updateDateTime,
        @JsonProperty("UpdateLocation") String updateLocation
    ) {}
    
    record AramexTrackingResponse(
        @JsonProperty("TrackingResults") List<AramexTrackingResult> trackingResults,
        @JsonProperty("HasErrors") boolean hasErrors,
        @JsonProperty("Notifications") List<AramexNotification> notifications
    ) {
        String errorMessage() {
            if (notifications != null && !notifications.isEmpty()) {
                return notifications.get(0).message();
            }
            return "Unknown tracking error";
        }
    }
    
    record AramexRateRequest(
        @JsonProperty("ClientInfo") AramexClientInfo clientInfo,
        @JsonProperty("Transaction") AramexTransaction transaction,
        @JsonProperty("DestinationAddress") AramexAddress destinationAddress,
        @JsonProperty("OriginAddress") AramexAddress originAddress,
        @JsonProperty("ShipmentDetails") AramexPackage shipmentDetails,
        @JsonProperty("ProductGroup") String productGroup
    ) {}
    
    record AramexRateResponse(
        @JsonProperty("TotalAmount") Double totalAmount,
        @JsonProperty("Currency") String currency,
        @JsonProperty("HasErrors") boolean hasErrors,
        @JsonProperty("Notifications") List<AramexNotification> notifications
    ) {
        String errorMessage() {
            if (notifications != null && !notifications.isEmpty()) {
                return notifications.get(0).message();
            }
            return "Unknown rate error";
        }
    }
}
