package com.mustapha.ecommerce.shared.external.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Tawk.to integration adapter
 * Provides live chat support for customers
 * 
 * Setup Instructions:
 * 1. Sign up at https://www.tawk.to (FREE)
 * 2. Get your Property ID from: Administration → Property Settings
 * 3. Get your Widget ID from: Administration → Channels → Chat Widget
 * 4. Get your API Key from: Administration → Property Settings → API Key
 * 5. Set environment variables:
 *    - TAWK_PROPERTY_ID
 *    - TAWK_API_KEY
 */
@Slf4j
@Component
public class TawkToSupportAdapter implements CustomerSupportPort {

    private final String propertyId;
    private final String apiKey;
    private final boolean enabled;

    public TawkToSupportAdapter(
        @Value("${tawk.property-id:}") String propertyId,
        @Value("${tawk.api-key:}") String apiKey
    ) {
        this.propertyId = propertyId;
        this.apiKey = apiKey;
        this.enabled = !propertyId.isBlank() && !apiKey.isBlank();

        if (enabled) {
            log.info("💬 Tawk.to support chat initialized (Property ID: {})", 
                propertyId.substring(0, Math.min(8, propertyId.length())) + "...");
        } else {
            log.warn("⚠️ Tawk.to not configured. Set TAWK_PROPERTY_ID and TAWK_API_KEY environment variables.");
            log.info("📝 Sign up at https://www.tawk.to (FREE) and get your credentials");
        }
    }

    @Override
    public ChatConfig initializeChatForCustomer(Long customerId, String customerName, String customerEmail) {
        if (!enabled) {
            log.warn("⚠️ Tawk.to not configured. Cannot initialize chat.");
            return new ChatConfig("", "", "", false, "Chat support is temporarily unavailable");
        }

        log.info("💬 Initializing chat for customer {} ({})", customerId, customerEmail);

        // Generate secure HMAC signature for user identification
        String secureHmac = generateHmac(customerEmail);

        return new ChatConfig(
            propertyId,
            propertyId, // Widget ID is same as property ID for simple setup
            secureHmac,
            true, // Assume online for now
            String.format("Hi %s! 👋 How can we help you today?", customerName)
        );
    }

    @Override
    public void createOfflineMessage(Long customerId, String message) {
        log.info("📝 Creating offline message from customer {}: {}", customerId, message);
        // TODO: Store offline messages in database
        // TODO: Send email notification to support team
    }

    @Override
    public String getChatTranscript(String chatId) {
        log.info("📄 Getting chat transcript for chat {}", chatId);
        // TODO: Call Tawk.to API to get chat transcript
        return "";
    }

    private String generateHmac(String userEmail) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(userEmail.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("❌ Failed to generate HMAC: {}", e.getMessage());
            return "";
        }
    }
}
