package com.mustapha.ecommerce.user.domain.model.valueobject;

/**
 * Domain service interface for password hashing.
 * Implementation will be provided by infrastructure layer (BCrypt, Argon2, etc.).
 * This keeps the domain pure and framework-agnostic.
 * 
 * Security Requirements for Implementation:
 * - MUST use adaptive hashing algorithm (BCrypt, Argon2, PBKDF2)
 * - MUST include salt (automatic in BCrypt/Argon2)
 * - BCrypt: Minimum 10 rounds (recommended 12 for 2026)
 * - Argon2: Follow OWASP recommendations for memory/iterations
 * - MUST be resistant to timing attacks
 * - MUST handle concurrent hashing safely
 * 
 * Pattern: Port/Adapter (Hexagonal Architecture)
 * Domain defines contract, infrastructure provides implementation
 */
public interface PasswordHasher {
    /**
     * Hashes a plain text password using a secure adaptive algorithm.
     * 
     * Implementation Requirements:
     * - Use BCrypt (recommended), Argon2, or PBKDF2
     * - Auto-generate unique salt per password
     * - BCrypt: 12 rounds minimum (balances security vs performance)
     * - Result format: algorithm-specific (BCrypt: $2a$12$...)
     * 
     * @param plainText the plain text password to hash (must not be null or blank)
     * @return the hashed password string (includes algorithm, cost, salt, hash)
     * @throws IllegalArgumentException if plainText is null or blank
     * @throws RuntimeException if hashing fails (infrastructure error)
     */
    String hash(String plainText);

    /**
     * Verifies if a plain text password matches a hashed password.
     * 
     * Security Requirements:
     * - MUST use constant-time comparison to prevent timing attacks
     * - BCrypt/Argon2 libraries handle this automatically
     * - MUST safely handle null inputs (return false, don't throw)
     * 
     * @param plainText the plain text password to verify (null-safe)
     * @param hashedPassword the hashed password to compare against (null-safe)
     * @return true if passwords match, false otherwise (including null inputs)
     */
    boolean matches(String plainText, String hashedPassword);

    /**
     * Checks if a hashed password needs to be rehashed with current security parameters.
     * 
     * Use Case: Automatic security upgrades during login
     * - User logs in with old hash (e.g., BCrypt 10 rounds)
     * - System detects outdated cost factor
     * - Automatically rehashes with current standard (12 rounds)
     * - Updates database transparently
     * 
     * Implementation Example (BCrypt):
     * - Current standard: 12 rounds
     * - Hash format: $2a$10$... (10 rounds - OLD)
     * - Returns: true (needs upgrade to 12 rounds)
     * 
     * @param hashedPassword the hashed password to check (null-safe)
     * @return true if password should be rehashed with current parameters, false otherwise
     */
    boolean needsRehash(String hashedPassword);

    /**
     * Returns the hashing algorithm identifier used by this implementation.
     * 
     * Use Cases:
     * - Audit logging (compliance requirement)
     * - Migration tracking (BCrypt → Argon2 transition)
     * - Security reports (algorithm inventory across users)
     * - Debugging and troubleshooting
     * 
     * Return Format Examples:
     * - "BCrypt" or "BCrypt-12" (algorithm + cost factor)
     * - "Argon2id" 
     * - "PBKDF2-SHA256"
     * 
     * @return algorithm identifier string (e.g., "BCrypt-12", "Argon2id")
     */
    String getAlgorithm();
}
