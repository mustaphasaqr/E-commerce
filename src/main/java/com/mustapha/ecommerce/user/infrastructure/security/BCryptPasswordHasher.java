package com.mustapha.ecommerce.user.infrastructure.security;

import com.mustapha.ecommerce.user.domain.model.valueobject.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt Password Hasher Implementation
 * Responsibility: Hash and verify passwords using BCrypt algorithm
 * Pattern: Adapter (Domain Port → Spring Security)
 * 
 * Scope: USER bounded context
 * Used by: User use cases (RegisterUserUseCase, ChangePasswordUseCase)
 * Injected into: Auth use cases (LoginUseCase for password verification)
 * 
 * Security:
 * - BCrypt strength: 12 rounds (recommended for 2026)
 * - Automatic salt generation
 * - Timing attack resistant
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasher() {
        // Strength 12 = 2^12 iterations (4096)
        // Balance between security and performance
        this.encoder = new BCryptPasswordEncoder(12);
    }

    @Override
    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return encoder.matches(plainPassword, hashedPassword);
    }

    @Override
    public boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        // BCrypt hash format: $2a$12$... (rounds is the 2nd segment)
        // Check if current hash uses lower rounds than our configured strength (12)
        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length >= 3) {
                int currentRounds = Integer.parseInt(parts[2]);
                return currentRounds < 12; // Need rehash if less than 12 rounds
            }
        } catch (Exception e) {
            // Invalid hash format - assume needs rehash
            return true;
        }
        return false;
    }

    @Override
    public String getAlgorithm() {
        return "BCrypt-12";
    }
}
