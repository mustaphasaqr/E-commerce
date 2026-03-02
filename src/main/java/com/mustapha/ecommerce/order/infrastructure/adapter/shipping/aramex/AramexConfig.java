package com.mustapha.ecommerce.order.infrastructure.adapter.shipping.aramex;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Aramex Shipping Configuration
 * Loads Aramex credentials from application.properties
 * 
 * Setup Instructions:
 * 1. Sign up at https://www.aramex.com/solutions/developers
 * 2. Get API credentials from Aramex developer portal
 * 3. Set environment variables:
 *    - ARAMEX_USERNAME=your_username
 *    - ARAMEX_PASSWORD=your_password
 *    - ARAMEX_ACCOUNT_NUMBER=your_account_number
 *    - ARAMEX_ACCOUNT_PIN=your_account_pin
 *    - ARAMEX_ACCOUNT_ENTITY=your_account_entity (e.g., DXB - Dubai)
 *    - ARAMEX_BASE_URL=https://ws.aramex.net/ShippingAPI.V2 (production)
 * 
 * Test Mode:
 * - Use test credentials from Aramex developer portal
 * - Test URL: https://ws.dev.aramex.net/ShippingAPI.V2
 * 
 * MENA Coverage:
 * - Egypt (EG) ✅
 * - United Arab Emirates (AE) ✅
 * - Saudi Arabia (SA) ✅
 * - Kuwait, Qatar, Bahrain, Oman, Jordan, Lebanon, etc.
 * 
 * Failover Strategy:
 * - If Aramex not configured: Falls back to MANUAL mode
 * - Admin enters tracking number manually
 * - Graceful degradation pattern (similar to SendGrid email, Accept payments)
 */
@Configuration
@ConfigurationProperties(prefix = "shipping.aramex")
public class AramexConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AramexConfig.class);
    
    private String username;
    private String password;
    private String accountNumber;
    private String accountPin;
    private String accountEntity = "DXB";  // Default to Dubai entity
    private String baseUrl = "https://ws.aramex.net/ShippingAPI.V2"; // Production by default
    
    // Optional: Default sender address (can be overridden per shipment)
    private String senderName;
    private String senderCompany;
    private String senderAddress1;
    private String senderAddress2;
    private String senderCity;
    private String senderState;
    private String senderPostalCode;
    private String senderCountry;
    private String senderPhone;
    private String senderEmail;
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountPin() {
        return accountPin;
    }
    
    public void setAccountPin(String accountPin) {
        this.accountPin = accountPin;
    }
    
    public String getAccountEntity() {
        return accountEntity;
    }
    
    public void setAccountEntity(String accountEntity) {
        this.accountEntity = accountEntity;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    // Sender address getters/setters
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getSenderCompany() {
        return senderCompany;
    }
    
    public void setSenderCompany(String senderCompany) {
        this.senderCompany = senderCompany;
    }
    
    public String getSenderAddress1() {
        return senderAddress1;
    }
    
    public void setSenderAddress1(String senderAddress1) {
        this.senderAddress1 = senderAddress1;
    }
    
    public String getSenderAddress2() {
        return senderAddress2;
    }
    
    public void setSenderAddress2(String senderAddress2) {
        this.senderAddress2 = senderAddress2;
    }
    
    public String getSenderCity() {
        return senderCity;
    }
    
    public void setSenderCity(String senderCity) {
        this.senderCity = senderCity;
    }
    
    public String getSenderState() {
        return senderState;
    }
    
    public void setSenderState(String senderState) {
        this.senderState = senderState;
    }
    
    public String getSenderPostalCode() {
        return senderPostalCode;
    }
    
    public void setSenderPostalCode(String senderPostalCode) {
        this.senderPostalCode = senderPostalCode;
    }
    
    public String getSenderCountry() {
        return senderCountry;
    }
    
    public void setSenderCountry(String senderCountry) {
        this.senderCountry = senderCountry;
    }
    
    public String getSenderPhone() {
        return senderPhone;
    }
    
    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }
    
    public String getSenderEmail() {
        return senderEmail;
    }
    
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }
    
    /**
     * Check if Aramex is properly configured
     * Requires: username, password, account number, account pin
     */
    public boolean isConfigured() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               accountNumber != null && !accountNumber.trim().isEmpty() &&
               accountPin != null && !accountPin.trim().isEmpty();
    }
    
    /**
     * Check if sender address is configured
     */
    public boolean hasSenderAddress() {
        return senderName != null && !senderName.trim().isEmpty() &&
               senderAddress1 != null && !senderAddress1.trim().isEmpty() &&
               senderCity != null && !senderCity.trim().isEmpty() &&
               senderCountry != null && !senderCountry.trim().isEmpty() &&
               senderPhone != null && !senderPhone.trim().isEmpty();
    }
    
    @PostConstruct
    public void init() {
        if (isConfigured()) {
            logger.info("✅ Aramex Shipping is configured (REAL mode)");
            logger.info("   Account Number: {}", accountNumber);
            logger.info("   Account Entity: {}", accountEntity);
            logger.info("   Base URL: {}", baseUrl);
            
            if (hasSenderAddress()) {
                logger.info("   Default Sender: {} - {}, {}", 
                           senderCompany, senderCity, senderCountry);
            } else {
                logger.warn("   ⚠️ No default sender address configured");
                logger.warn("      Sender address must be provided per shipment");
            }
        } else {
            logger.warn("⚠️ Aramex Shipping is NOT configured (MANUAL mode)");
            logger.warn("   Shipping will use manual tracking number entry");
            logger.warn("   To enable Aramex API:");
            logger.warn("   - Set ARAMEX_USERNAME, ARAMEX_PASSWORD");
            logger.warn("   - Set ARAMEX_ACCOUNT_NUMBER, ARAMEX_ACCOUNT_PIN");
        }
    }
    
    /**
     * Validate configuration (throws if invalid)
     */
    public void validate() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                "Aramex configuration incomplete. Set ARAMEX_USERNAME, ARAMEX_PASSWORD, " +
                "ARAMEX_ACCOUNT_NUMBER, and ARAMEX_ACCOUNT_PIN environment variables."
            );
        }
    }
}
