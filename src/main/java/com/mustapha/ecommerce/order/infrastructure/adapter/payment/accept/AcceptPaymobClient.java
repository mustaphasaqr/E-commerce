package com.mustapha.ecommerce.order.infrastructure.adapter.payment.accept;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mustapha.ecommerce.order.infrastructure.adapter.payment.sdk.PaymentGatewayClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accept (Paymob) HTTP Client
 * Responsibility: Make HTTP requests to Accept (Paymob) API
 * 
 * Accept API Documentation:
 * https://docs.paymob.com/docs/accept-standard-redirect
 * 
 * Payment Flow (3-Step):
 * 1. POST /auth/tokens → Get authentication token
 * 2. POST /ecommerce/orders → Register order
 * 3. POST /acceptance/payment_keys → Get payment key
 * 4. Redirect customer to: /acceptance/iframes/{iframe_id}?payment_token={payment_key}
 * 5. Customer completes payment
 * 6. Accept sends callback to your webhook
 * 7. GET /acceptance/transactions/{id} → Verify payment status
 * 
 * Features:
 * - Idempotency support (prevents duplicate checkouts)
 * - Graceful degradation (MOCK mode if not configured)
 * - Thread-safe concurrent idempotency store
 * - Token caching (30 min validity)
 */
@Component
public class AcceptPaymobClient implements PaymentGatewayClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AcceptPaymobClient.class);
    
    private final AcceptPaymobConfig config;
    private final RestTemplate restTemplate;
    
    // In-memory idempotency store (production: use Redis/Database)
    private final Map<String, String> idempotencyStore = new ConcurrentHashMap<>();
    
    // Token cache (Accept tokens expire after ~30 minutes)
    private String cachedAuthToken = null;
    private long tokenExpiryTime = 0;
    
    private boolean isRealModeEnabled = false;
    
    public AcceptPaymobClient(AcceptPaymobConfig config, RestTemplateBuilder builder) {
        this.config = config;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    @PostConstruct
    public void init() {
        isRealModeEnabled = config.isConfigured();
        
        if (isRealModeEnabled) {
            logger.info("✅ AcceptPaymobClient initialized (REAL mode)");
        } else {
            logger.warn("⚠️ AcceptPaymobClient initialized in MOCK mode");
            logger.warn("   Payments will be simulated - set ACCEPT_* env vars for real payments");
        }
    }
    
    /**
     * Create checkout session (3-step process)
     * Returns payment key that customer uses to complete payment
     * 
     * Idempotent: Same orderId returns same payment key
     */
    public CheckoutResponse createCheckout(
            String orderId,
            double amount,
            String currency,
            String customerEmail,
            String customerPhone) {
        
        // Check idempotency
        String idempotencyKey = "checkout_" + orderId;
        String existingPaymentKey = idempotencyStore.get(idempotencyKey);
        if (existingPaymentKey != null) {
            logger.info("Idempotent checkout: returning existing paymentKey for orderId={}", orderId);
            return new CheckoutResponse(existingPaymentKey, null, 1800); // 30min expiry
        }
        
        if (!isRealModeEnabled) {
            return createMockCheckout(orderId, idempotencyKey);
        }
        
        try {
            // Step 1: Get auth token
            String authToken = getAuthToken();
            if (authToken == null) {
                return new CheckoutResponse(null, "Failed to authenticate with Accept", 0);
            }
            
            // Step 2: Register order
            String acceptOrderId = registerOrder(authToken, orderId, amount, currency);
            if (acceptOrderId == null) {
                return new CheckoutResponse(null, "Failed to register order with Accept", 0);
            }
            
            // Step 3: Get payment key
            String paymentKey = getPaymentKey(authToken, acceptOrderId, amount, currency, customerEmail, customerPhone);
            if (paymentKey == null) {
                return new CheckoutResponse(null, "Failed to get payment key from Accept", 0);
            }
            
            // Store for idempotency
            idempotencyStore.put(idempotencyKey, paymentKey);
            
            logger.info("✅ Checkout created: orderId={}, paymentKey={}", orderId, paymentKey);
            return new CheckoutResponse(paymentKey, null, 1800); // 30 min expiry
            
        } catch (Exception e) {
            logger.error("❌ Accept checkout error: orderId={}, error={}", orderId, e.getMessage(), e);
            return new CheckoutResponse(null, "Checkout creation failed: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Step 1: Authenticate and get token
     * Tokens are cached for 30 minutes
     */
    private String getAuthToken() {
        // Return cached token if still valid
        if (cachedAuthToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            logger.debug("Using cached auth token");
            return cachedAuthToken;
        }
        
        try {
            String url = config.getBaseUrl() + "/auth/tokens";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("api_key", config.getApiKey());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
            );
            
            logger.info("Auth response: status={}, body={}", rawResponse.getStatusCode(), rawResponse.getBody());
            
            if (rawResponse.getStatusCode().is2xxSuccessful() && rawResponse.getBody() != null) {
                // Parse the token from JSON response
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(rawResponse.getBody());
                String token = node.has("token") ? node.get("token").asText() : null;
                
                if (token == null || token.isEmpty()) {
                    logger.error("❌ Auth token missing in response: {}", rawResponse.getBody());
                    return null;
                }
                
                // Cache token for 25 minutes (5 min buffer before 30 min expiry)
                cachedAuthToken = token;
                tokenExpiryTime = System.currentTimeMillis() + (25 * 60 * 1000);
                
                logger.debug("✅ Auth token obtained");
                return token;
            } else {
                logger.error("❌ Failed to get auth token: status={}, body={}", rawResponse.getStatusCode(), rawResponse.getBody());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("❌ Auth API error: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Step 2: Register order with Accept
     * Handles 422 "duplicate" by retrying with a suffixed merchant_order_id
     */
    private String registerOrder(String authToken, String orderId, double amount, String currency) {
        // Try with original orderId first, then with retry suffix if duplicate
        String[] merchantOrderIds = { orderId, orderId + "-retry-" + System.currentTimeMillis() };
        
        for (String merchantOrderId : merchantOrderIds) {
            try {
                String url = config.getBaseUrl() + "/ecommerce/orders";
                
                // Convert amount to cents (Accept expects integer in cents)
                int amountCents = (int) (amount * 100);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("auth_token", authToken);
                requestBody.put("delivery_needed", "false");
                requestBody.put("amount_cents", String.valueOf(amountCents));
                requestBody.put("currency", currency);
                requestBody.put("merchant_order_id", merchantOrderId);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                logger.info("Registering order with Accept: merchantOrderId={}, amountCents={}", merchantOrderId, amountCents);
                
                ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class
                );
                
                logger.info("Order registration response: status={}, body={}", rawResponse.getStatusCode(), rawResponse.getBody());
                
                if (rawResponse.getStatusCode().is2xxSuccessful() && rawResponse.getBody() != null) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(rawResponse.getBody());
                    long ordId = node.has("id") ? node.get("id").asLong() : 0;
                    if (ordId == 0) {
                        logger.error("❌ Order ID missing in response: {}", rawResponse.getBody());
                        return null;
                    }
                    String acceptOrderId = String.valueOf(ordId);
                    logger.info("✅ Order registered: acceptOrderId={}", acceptOrderId);
                    return acceptOrderId;
                } else {
                    logger.error("❌ Failed to register order: status={}, body={}", rawResponse.getStatusCode(), rawResponse.getBody());
                    return null;
                }
                
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                if (e.getStatusCode().value() == 422 && e.getResponseBodyAsString().contains("duplicate")) {
                    logger.warn("⚠️ Duplicate merchant_order_id={}, retrying with suffix...", merchantOrderId);
                    continue; // Try next merchantOrderId with suffix
                }
                logger.error("❌ Order registration HTTP error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
                return null;
            } catch (Exception e) {
                logger.error("❌ Order registration error: {}", e.getMessage(), e);
                return null;
            }
        }
        
        logger.error("❌ Order registration failed after retry");
        return null;
    }
    
    /**
     * Step 3: Get payment key
     */
    private String getPaymentKey(
            String authToken, 
            String acceptOrderId, 
            double amount, 
            String currency,
            String customerEmail,
            String customerPhone) {
        try {
            String url = config.getBaseUrl() + "/acceptance/payment_keys";
            
            // Convert amount to cents
            int amountCents = (int) (amount * 100);
            
            // Build billing data
            Map<String, String> billingData = new HashMap<>();
            billingData.put("email", customerEmail != null ? customerEmail : "customer@example.com");
            billingData.put("first_name", "Customer");
            billingData.put("last_name", "User");
            billingData.put("phone_number", customerPhone != null ? customerPhone : "+20" + "1000000000");
            billingData.put("country", "EG");
            billingData.put("city", "Cairo");
            billingData.put("street", "N/A");
            billingData.put("building", "N/A");
            billingData.put("floor", "N/A");
            billingData.put("apartment", "N/A");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("auth_token", authToken);
            requestBody.put("amount_cents", String.valueOf(amountCents));
            requestBody.put("expiration", 3600); // 1 hour expiry
            requestBody.put("order_id", acceptOrderId);
            requestBody.put("billing_data", billingData);
            requestBody.put("currency", currency);
            requestBody.put("integration_id", Integer.parseInt(config.getIntegrationId()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            logger.info("Getting payment key: url={}", url);
            
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
            );
            
            logger.info("Payment key response: status={}, body length={}", rawResponse.getStatusCode(), 
                        rawResponse.getBody() != null ? rawResponse.getBody().length() : 0);
            
            if (rawResponse.getStatusCode().is2xxSuccessful() && rawResponse.getBody() != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(rawResponse.getBody());
                String paymentKey = node.has("token") ? node.get("token").asText() : null;
                if (paymentKey == null || paymentKey.isEmpty()) {
                    logger.error("❌ Payment key missing in response: {}", rawResponse.getBody());
                    return null;
                }
                logger.debug("✅ Payment key obtained");
                return paymentKey;
            } else {
                logger.error("❌ Failed to get payment key: status={}, body={}", rawResponse.getStatusCode(), rawResponse.getBody());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("❌ Payment key error: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Verify payment status after customer completes payment
     */
    public PaymentVerificationResponse verifyPayment(String transactionId) {
        if (!isRealModeEnabled) {
            return createMockVerification(transactionId);
        }
        
        try {
            String authToken = getAuthToken();
            if (authToken == null) {
                return new PaymentVerificationResponse(false, "AUTH_FAILED", null, null);
            }
            
            String url = config.getBaseUrl() + "/acceptance/transactions/" + transactionId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<TransactionResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, request, TransactionResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TransactionResponse tx = response.getBody();
                boolean success = tx.success();
                
                logger.info("Payment verification: transactionId={}, success={}", transactionId, success);
                
                return new PaymentVerificationResponse(
                    success,
                    success ? "PAID" : "FAILED",
                    transactionId,
                    String.valueOf(tx.orderId())
                );
            } else {
                logger.error("❌ Verification failed: status={}", response.getStatusCode());
                return new PaymentVerificationResponse(false, "VERIFICATION_FAILED", transactionId, null);
            }
            
        } catch (RestClientException e) {
            logger.error("❌ Verification error: transactionId={}, error={}", transactionId, e.getMessage());
            return new PaymentVerificationResponse(false, "ERROR", transactionId, null);
        }
    }
    
    /**
     * MOCK mode: Create fake checkout for testing
     */
    private CheckoutResponse createMockCheckout(String orderId, String idempotencyKey) {
        String mockPaymentKey = "txn_mock_" + UUID.randomUUID().toString().substring(0, 8);
        idempotencyStore.put(idempotencyKey, mockPaymentKey);
        
        logger.info("🎭 MOCK checkout created: orderId={}, paymentKey={}", orderId, mockPaymentKey);
        return new CheckoutResponse(mockPaymentKey, null, 1800);
    }
    
    /**
     * MOCK mode: Create fake verification
     */
    private PaymentVerificationResponse createMockVerification(String transactionId) {
        logger.info("🎭 MOCK verification: transactionId={}, status=PAID", transactionId);
        return new PaymentVerificationResponse(true, "PAID", transactionId, "mock_order_id");
    }
    
    // PaymentGatewayClient interface implementation (for testing)
    
    /**
     * Charge payment with idempotency key
     * Simplified wrapper around createCheckout for testing purposes
     */
    @Override
    public String chargeWithIdempotency(double amount, String paymentToken, String idempotencyKey) {
        // Use computeIfAbsent for thread-safe idempotency
        return idempotencyStore.computeIfAbsent(idempotencyKey, key -> {
            logger.debug("Idempotency miss: key={}, creating new charge", idempotencyKey);
            
            try {
                // Generate transaction ID directly (don't go through createCheckout to avoid double idempotency)
                String txnId;
                if (!isRealModeEnabled) {
                    // MOCK mode: generate mock transaction ID
                    txnId = "txn_mock_" + UUID.randomUUID().toString().substring(0, 8);
                    logger.info("🎭 MOCK charge: key={}, txnId={}", idempotencyKey, txnId);
                } else {
                    // Real mode: create checkout and return payment key as transaction ID
                    CheckoutResponse response = createCheckout(
                        idempotencyKey,  // Use idempotency key as order ID
                        amount,
                        "EGP",
                        "test@example.com",
                        null
                    );
                    
                    if (response.paymentKey() != null) {
                        txnId = response.paymentKey();
                    } else {
                        throw new RuntimeException("Payment failed: " + response.error());
                    }
                }
                
                return txnId;
                
            } catch (Exception e) {
                logger.error("Charge failed: key={}, error={}", idempotencyKey, e.getMessage());
                throw new RuntimeException("Payment charge failed: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Refund payment with idempotency key
     * Accept doesn't have direct refund API - refunds done via dashboard
     * This is a mock implementation for testing
     */
    @Override
    public String refundWithIdempotency(String transactionId, double amount, String idempotencyKey) {
        // Check idempotency store first (use separate namespace for refunds)
        String refundKey = "refund_" + idempotencyKey;
        if (idempotencyStore.containsKey(refundKey)) {
            String cachedResult = idempotencyStore.get(refundKey);
            logger.debug("Refund idempotency hit: key={}, result={}", idempotencyKey, cachedResult);
            return cachedResult;
        }
        
        try {
            // Generate refund ID (in real implementation, call Accept API)
            String refundId = "refund_" + UUID.randomUUID().toString().substring(0, 8);
            
            logger.info("Refund processed: txnId={}, amount={}, refundId={}", 
                       transactionId, amount, refundId);
            
            // Store in idempotency cache
            idempotencyStore.put(refundKey, refundId);
            return refundId;
            
        } catch (Exception e) {
            logger.error("Refund failed: key={}, error={}", idempotencyKey, e.getMessage());
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clear idempotency store (for testing)
     */
    @Override
    public void clearIdempotencyStore() {
        idempotencyStore.clear();
        logger.debug("Idempotency store cleared");
    }
    
    // API Response DTOs (internal use only)
    private record AuthTokenResponse(@JsonProperty("token") String token) {}
    private record OrderResponse(@JsonProperty("id") long id) {}
    private record PaymentKeyResponse(@JsonProperty("token") String token) {}
    private record TransactionResponse(
        @JsonProperty("success") boolean success,
        @JsonProperty("id") long id,
        @JsonProperty("order") long orderId
    ) {}
}
