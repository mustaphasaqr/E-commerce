package com.mustapha.ecommerce.shared.config;

import com.twilio.Twilio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * SMS Service Configuration (Twilio)
 * 
 * Setup Instructions:
 * 1. Sign up at: https://www.twilio.com/try-twilio
 * 2. Free Trial includes:
 *    - $15 trial credit (sends ~500 SMS)
 *    - Test phone number
 *    - SMS to verified numbers only in trial mode
 * 
 * 3. Get Credentials from Dashboard:
 *    - Account SID: https://console.twilio.com
 *    - Auth Token: https://console.twilio.com
 *    - Phone Number: Your Twilio phone number (e.g., +15551234567)
 * 
 * 4. Verify Phone Numbers (Trial Mode):
 *    - Go to: Phone Numbers → Verified Caller IDs
 *    - Add your phone number for testing
 * 
 * 5. Set Environment Variables:
 *    - TWILIO_ACCOUNT_SID=your_account_sid
 *    - TWILIO_AUTH_TOKEN=your_auth_token
 *    - TWILIO_PHONE_NUMBER=+15551234567
 * 
 * Use Cases:
 * - OTP (One-Time Password) for login verification
 * - Order confirmation notifications
 * - Shipping updates
 * - Password reset codes
 * 
 * SMS Templates:
 * - OTP: "Your verification code is: {code}. Valid for 5 minutes."
 * - Order: "Order #{orderId} confirmed! Total: {amount} EGP. Track: {link}"
 * - Shipping: "Your order #{orderId} has been shipped. Estimated delivery: {date}"
 * 
 * Alternative for Egypt:
 * - VictoryLink: https://www.victorylink.com (Egyptian SMS provider)
 * - OrangeSMS: https://developer.orange.com/egypt
 * 
 * Pricing (After Trial):
 * - Egypt: ~$0.04 per SMS
 * - USA: ~$0.0079 per SMS
 * - International: Varies by country
 */
@Configuration
public class SmsConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsConfig.class);
    
    @Value("${sms.twilio.account-sid:#{null}}")
    private String accountSid;
    
    @Value("${sms.twilio.auth-token:#{null}}")
    private String authToken;
    
    @Value("${sms.twilio.phone-number:#{null}}")
    private String fromPhoneNumber;
    
    @Value("${sms.enabled:true}")
    private boolean smsEnabled;
    
    @PostConstruct
    public void initialize() {
        if (!smsEnabled) {
            logger.warn("⚠️ SMS service DISABLED via configuration");
            return;
        }
        
        if (!isConfigured()) {
            logger.warn("⚠️ Twilio credentials not configured. SMS service will use MOCK mode.");
            logger.warn("   For production, set environment variables:");
            logger.warn("   - TWILIO_ACCOUNT_SID=your_account_sid");
            logger.warn("   - TWILIO_AUTH_TOKEN=your_auth_token");
            logger.warn("   - TWILIO_PHONE_NUMBER=+15551234567");
            logger.warn("   Sign up for free trial: https://www.twilio.com/try-twilio");
            return;
        }
        
        try {
            Twilio.init(accountSid, authToken);
            logger.info("✅ Twilio SMS service configured");
            logger.info("   From Number: {}", maskPhoneNumber(fromPhoneNumber));
            logger.info("   Account SID ends with: {}", 
                       accountSid.substring(Math.max(0, accountSid.length() - 4)));
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Twilio: {}", e.getMessage());
            logger.warn("   SMS service will use MOCK mode");
        }
    }
    
    public String getAccountSid() {
        return accountSid;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }
    
    public boolean isSmsEnabled() {
        return smsEnabled;
    }
    
    public boolean isConfigured() {
        return accountSid != null && !accountSid.isBlank() &&
               authToken != null && !authToken.isBlank() &&
               fromPhoneNumber != null && !fromPhoneNumber.isBlank();
    }
    
    /**
     * Mask phone number for security (e.g., +155****4567)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return phoneNumber;
        }
        int visibleDigits = 4;
        int startVisible = Math.min(3, phoneNumber.length() - visibleDigits - 4);
        return phoneNumber.substring(0, startVisible) + 
               "****" + 
               phoneNumber.substring(phoneNumber.length() - visibleDigits);
    }
}
