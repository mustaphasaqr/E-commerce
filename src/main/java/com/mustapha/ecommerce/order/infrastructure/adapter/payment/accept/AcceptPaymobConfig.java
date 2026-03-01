package com.mustapha.ecommerce.order.infrastructure.adapter.payment.accept;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Accept (Paymob) Configuration
 * Loads Accept credentials from application.properties
 * 
 * Setup Instructions:
 * 1. Sign up at https://accept.paymob.com/portal/en/register
 * 2. Get API Key, Integration ID from dashboard
 * 3. Set environment variables:
 *    - ACCEPT_API_KEY=your_api_key
 *    - ACCEPT_INTEGRATION_ID=your_integration_id
 *    - ACCEPT_BASE_URL=https://accept.paymob.com/api (production)
 * 
 * Test Mode:
 * - Same credentials work for test and production
 * - Use test card numbers provided in dashboard
 * - Standard test cards:
 *   - VISA: 4987654321098769, CVV: 123, Expiry: 05/2025
 *   - Mastercard: 5123456789012346, CVV: 123, Expiry: 05/2025
 */
@Configuration
@ConfigurationProperties(prefix = "payment.accept")
public class AcceptPaymobConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AcceptPaymobConfig.class);
    
    private String apiKey;
    private String integrationId;
    private String baseUrl = "https://accept.paymob.com/api"; // Default production URL
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getIntegrationId() {
        return integrationId;
    }
    
    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    /**
     * Check if Accept is properly configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() &&
               integrationId != null && !integrationId.trim().isEmpty();
    }
    
    @PostConstruct
    public void init() {
        if (isConfigured()) {
            logger.info("✅ Accept (Paymob) is configured");
            logger.info("   Integration ID: {}", integrationId);
            logger.info("   Base URL: {}", baseUrl);
        } else {
            logger.warn("⚠️ Accept (Paymob) is NOT configured");
            logger.warn("   Set ACCEPT_API_KEY and ACCEPT_INTEGRATION_ID for real payments");
        }
    }
    
    /**
     * Validate configuration on startup
     */
    public void validate() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                "Accept configuration incomplete. Set ACCEPT_API_KEY and ACCEPT_INTEGRATION_ID"
            );
        }
    }
}
