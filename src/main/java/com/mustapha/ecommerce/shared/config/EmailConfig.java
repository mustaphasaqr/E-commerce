package com.mustapha.ecommerce.shared.config;

import com.sendgrid.SendGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Email Service Configuration
 * Provider: SendGrid
 * 
 * Setup Instructions:
 * 1. Sign up at https://sendgrid.com (FREE tier: 100 emails/day)
 * 2. Navigate to Settings > API Keys
 * 3. Create API Key with "Mail Send" permission
 * 4. Set environment variable: SENDGRID_API_KEY=your_api_key
 * 
 * Alternative: Use application.properties:
 * email.sendgrid.api-key=your_api_key
 * email.sendgrid.from-email=noreply@yourdomain.com
 * email.sendgrid.from-name=E-Commerce Platform
 * 
 * Production Checklist:
 * - Verify sender email in SendGrid dashboard
 * - Set up custom domain (DKIM, SPF records)
 * - Monitor SendGrid dashboard for delivery rates
 * - Set up webhook for bounce/spam reports
 */
@Configuration
public class EmailConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);
    
    @Value("${email.sendgrid.api-key:#{null}}")
    private String sendGridApiKey;
    
    @Value("${email.sendgrid.from-email:noreply@ecommerce.com}")
    private String fromEmail;
    
    @Value("${email.sendgrid.from-name:E-Commerce Platform}")
    private String fromName;
    
    @Value("${email.enabled:true}")
    private boolean emailEnabled;
    
    @Bean
    public SendGrid sendGrid() {
        if (!emailEnabled) {
            logger.warn("⚠️ Email service DISABLED via configuration");
            return null;
        }
        
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            logger.warn("⚠️ SendGrid API key not configured. Email service will use MOCK mode.");
            logger.warn("   Set environment variable: SENDGRID_API_KEY=your_api_key");
            logger.warn("   Or set in application.properties: email.sendgrid.api-key=your_api_key");
            return null;
        }
        
        logger.info("✅ SendGrid client initialized with API key ending in: {}",
                   sendGridApiKey.substring(Math.max(0, sendGridApiKey.length() - 4)));
        logger.info("   From Email: {} <{}>", fromName, fromEmail);
        
        return new SendGrid(sendGridApiKey);
    }
    
    public String getFromEmail() {
        return fromEmail;
    }
    
    public String getFromName() {
        return fromName;
    }
    
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
}
