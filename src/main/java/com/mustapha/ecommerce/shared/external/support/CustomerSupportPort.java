package com.mustapha.ecommerce.shared.external.support;

/**
 * Port for customer support chat integration (Tawk.to)
 */
public interface CustomerSupportPort {

    /**
     * Initialize chat for a customer
     * @return Chat widget configuration
     */
    ChatConfig initializeChatForCustomer(Long customerId, String customerName, String customerEmail);

    /**
     * Create offline message when support is unavailable
     */
    void createOfflineMessage(Long customerId, String message);

    /**
     * Get chat transcript
     */
    String getChatTranscript(String chatId);

    record ChatConfig(
        String propertyId,
        String widgetId,
        String secureHmac,  // HMAC signature for secure mode
        boolean isOnline,
        String welcomeMessage
    ) {}
}
