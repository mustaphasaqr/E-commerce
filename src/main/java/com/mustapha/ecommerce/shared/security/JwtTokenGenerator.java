package com.mustapha.ecommerce.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT Token Generator
 * 
 * Responsibilities:
 * - Generate access tokens (short-lived, 1 hour)
 * - Validate token signature and expiration
 * - Extract claims (userId, role)
 * 
 * Security:
 * - Uses HMAC-SHA256 (HS256) algorithm
 * - Secret key must be 256-bit minimum
 * - Tokens are stateless (no server-side storage)
 * 
 * Pattern: Adapter for Auth domain JwtGenerator port
 */
@Component
public class JwtTokenGenerator {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenGenerator(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generate Access Token
     * 
     * Claims:
     * - sub: userId (subject)
     * - role: user role (CUSTOMER, OWNER)
     * - sessionId: login session ID for logout functionality
     * - iat: issued at timestamp
     * - exp: expiration timestamp
     * 
     * @param userId User ID (UUID as string)
     * @param role User role (CUSTOMER, OWNER)
     * @param sessionId Login session ID (for logout)
     * @return JWT access token
     */
    public String generateAccessToken(String userId, String role, String sessionId) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("sessionId", sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate Token
     * 
     * Checks:
     * - Signature validity
     * - Expiration time
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Invalid signature, malformed JWT, or expired
            return false;
        }
    }

    /**
     * Extract User ID from token
     * 
     * @param token JWT token
     * @return userId (subject claim)
     */
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract Role from token
     * 
     * @param token JWT token
     * @return role (custom claim)
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Extract Session ID from token
     * 
     * @param token JWT token
     * @return sessionId (custom claim)
     */
    public String extractSessionId(String token) {
        return extractClaims(token).get("sessionId", String.class);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
