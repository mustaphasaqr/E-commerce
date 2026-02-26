package com.mustapha.ecommerce.user.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password Breach Checker using Have I Been Pwned (HIBP) API
 * 
 * Checks if a password has appeared in known data breaches.
 * Uses Troy Hunt's free Have I Been Pwned API: https://haveibeenpwned.com/API/v3
 * 
 * How It Works (k-Anonymity Model):
 * 1. Hash password with SHA-1 locally
 * 2. Send first 5 characters of hash to API
 * 3. API returns all suffixes for that prefix (800-1000 matches)
 * 4. Check locally if our full hash is in the list
 * 5. Privacy: Your password never leaves your system!
 * 
 * Example Flow:
 * Password: "password123"
 * SHA-1 Hash: "482C811DA5D5B4BC6D497FFA98491E38"
 * Send: "482C8" → API
 * Receive: ~800 hash suffixes starting with "482C8"
 * Check: Is "11DA5D5B4BC6D497FFA98491E38" in the list? Yes → Breached
 * 
 * API Details:
 * - Endpoint: https://api.pwnedpasswords.com/range/{first5HashChars}
 * - Rate Limit: None (generous, but respect their service)
 * - Cost: FREE (donations welcome)
 * - No API key required
 * - Privacy-preserving (k-anonymity)
 * 
 * Why SHA-1 (Not BCrypt)?
 * - API uses SHA-1 because it's fast for matching
 * - We still use BCrypt for actual password storage
 * - SHA-1 is fine here (we're not storing, just checking)
 * 
 * Fallback Strategy:
 * - If API is unreachable → Allow password (don't block user)
 * - Log error for monitoring
 * - Consider implementing circuit breaker for production
 */
@Service
public class PasswordBreachChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordBreachChecker.class);
    
    private static final String HIBP_API_URL = "https://api.pwnedpasswords.com/range/";
    
    private final RestTemplate restTemplate;
    
    public PasswordBreachChecker() {
        this.restTemplate = new RestTemplate();
        
        // Set user agent (HIBP API requests this for analytics)
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "E-Commerce-App-Password-Checker");
            return execution.execute(request, body);
        });
    }
    
    /**
     * Checks if password has been exposed in known data breaches.
     * 
     * @param password Plain text password
     * @return true if password found in breach database (should warn user)
     */
    public boolean isPasswordBreached(String password) {
        if (password == null || password.isBlank()) {
            return false; // Let other validators handle null/blank
        }
        
        try {
            // Step 1: Hash password with SHA-1
            String sha1Hash = sha1Hash(password);
            
            // Step 2: Split hash into prefix (first 5 chars) and suffix (remaining)
            String prefix = sha1Hash.substring(0, 5);
            String suffix = sha1Hash.substring(5);
            
            // Step 3: Query HIBP API with prefix
            String apiUrl = HIBP_API_URL + prefix;
            String response = restTemplate.getForObject(apiUrl, String.class);
            
            if (response == null || response.isBlank()) {
                return false; // No matches
            }
            
            // Step 4: Check if our suffix is in the response
            // Response format: "SUFFIX:COUNT\r\nSUFFIX:COUNT\r\n..."
            // Example: "11DA5D5B4BC6D497FFA98491E38:3861493\r\n..."
            String[] lines = response.split("\r\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String hashSuffix = parts[0];
                    int count = Integer.parseInt(parts[1]);
                    
                    if (hashSuffix.equalsIgnoreCase(suffix)) {
                        logger.warn("Password found in {} data breaches", count);
                        return true; // Password is breached!
                    }
                }
            }
            
            return false; // Password not found in breaches
            
        } catch (HttpClientErrorException e) {
            // API returned error (4xx, 5xx)
            logger.error("HIBP API error ({}): {}", e.getStatusCode(), e.getMessage());
            return false; // Fallback: allow password (don't block user due to API issues)
            
        } catch (ResourceAccessException e) {
            // Network error (timeout, DNS failure, etc.)
            logger.error("Cannot reach HIBP API: {}", e.getMessage());
            return false; // Fallback: allow password
            
        } catch (Exception e) {
            // Unexpected error (parsing, etc.)
            logger.error("Unexpected error checking password breach: {}", e.getMessage(), e);
            return false; // Fallback: allow password
        }
    }
    
    /**
     * Validates password is not breached.
     * Throws exception with helpful message if breached.
     * 
     * Note: This is a warning, not a hard block. User can proceed if they insist.
     * Consider making this a soft warning in UI instead of hard error.
     * 
     * @param password Plain text password
     * @throws IllegalArgumentException if password found in data breaches
     */
    public void validateNotBreached(String password) {
        if (isPasswordBreached(password)) {
            throw new IllegalArgumentException(
                "This password has been exposed in a data breach and is not safe to use. " +
                "Please choose a different password. " +
                "Learn more: https://haveibeenpwned.com/"
            );
        }
    }
    
    /**
     * Computes SHA-1 hash of input string.
     * 
     * @param input String to hash
     * @return Hex-encoded SHA-1 hash (uppercase)
     * @throws RuntimeException if SHA-1 algorithm not available (should never happen)
     */
    private String sha1Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }
    
    /**
     * Test method to verify API connectivity.
     * Checks if "password" (most common leaked password) is detected.
     * 
     * @return true if API is working correctly
     */
    public boolean testApiConnectivity() {
        try {
            boolean isBreached = isPasswordBreached("password");
            logger.info("HIBP API test: 'password' is {} breached", isBreached ? "" : "NOT");
            return isBreached; // "password" should always be breached
        } catch (Exception e) {
            logger.error("HIBP API connectivity test failed: {}", e.getMessage());
            return false;
        }
    }
}
