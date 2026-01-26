package com.mustapha.ecommerce.user.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a user password.
 * CRITICAL: Enforces password strength requirements and encapsulates hashed value.
 * Never stores passwords in plain text!
 * Pure domain - no framework dependencies.
 * 
 * Password Strength Requirements:
 * - Minimum 8 characters, maximum 128 characters (supports password managers)
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - At least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
 * 
 * Character Set Restriction:
 * - ASCII letters, digits, and common special characters only
 * - Unicode/emoji not supported (intentional - security best practice)
 * - Prevents encoding issues, database compatibility problems
 * 
 * Security Note: Uses PasswordHasher abstraction for hashing algorithm flexibility
 */
public class Password {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;  // Supports password managers (often generate 64-128 chars)
    
    // Lookahead patterns for individual requirements
    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
    
    // Allowed character set (intentionally restricted to ASCII)
    // Rationale: Prevents encoding issues, ensures DB compatibility, security best practice
    // Unicode/emoji passwords not supported by design
    private static final Pattern ALLOWED_CHARS = Pattern.compile(
        "^[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]+$"
    );

    private final String hashedValue;

    private Password(String hashedValue) {
        this.hashedValue = Objects.requireNonNull(hashedValue, "Hashed password cannot be null");
    }

    /**
     * Creates a Password from a plain text password, hashing it using the provided hasher.
     * Validates password strength before hashing.
     */
    public static Password fromPlainText(String plainText, PasswordHasher hasher) {
        Objects.requireNonNull(hasher, "Password hasher cannot be null");
        validatePasswordStrength(plainText);
        String hashed = hasher.hash(plainText);
        return new Password(hashed);
    }

    /**
     * Creates a Password from an already hashed password (e.g., from database).
     * Use this when loading existing users, NOT for new passwords!
     */
    public static Password fromHashed(String hashedValue) {
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be null or blank");
        }
        return new Password(hashedValue);
    }

    /**
     * Public validation method for password strength.
     * Useful for UI/API validation before attempting to create Password object.
     * 
     * @param plainText Password to validate
     * @throws IllegalArgumentException with specific error message if validation fails
     */
    public static void validate(String plainText) {
        validatePasswordStrength(plainText);
    }

    /**
     * Validates password strength requirements with detailed error messages.
     */
    private static void validatePasswordStrength(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        if (plainText.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "Password must be at least " + MIN_LENGTH + " characters long"
            );
        }

        if (plainText.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Password cannot exceed " + MAX_LENGTH + " characters"
            );
        }

        // Check each requirement individually for specific error messages
        if (!HAS_LOWERCASE.matcher(plainText).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least one lowercase letter (a-z)"
            );
        }

        if (!HAS_UPPERCASE.matcher(plainText).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter (A-Z)"
            );
        }

        if (!HAS_DIGIT.matcher(plainText).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least one digit (0-9)"
            );
        }

        if (!HAS_SPECIAL.matcher(plainText).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)"
            );
        }

        if (!ALLOWED_CHARS.matcher(plainText).matches()) {
            throw new IllegalArgumentException(
                "Password contains invalid characters. Only letters, digits, and special characters (!@#$%^&*()_+-=[]{}|;:,.<>?) are allowed"
            );
        }
    }

    /**
     * Checks if the provided plain text password matches this hashed password.
     * Uses the hasher's secure comparison to prevent timing attacks.
     */
    public boolean matches(String plainText, PasswordHasher hasher) {
        if (plainText == null || hasher == null) {
            return false;
        }
        return hasher.matches(plainText, hashedValue);
    }

    /**
     * Checks if this password needs to be rehashed with current security parameters.
     * 
     * Use Case: Automatic security upgrades during successful login
     * Example Flow:
     * 1. User logs in successfully (password matches)
     * 2. Check if password.needsRehash(hasher)
     * 3. If true: rehash with Password.fromPlainText(plainText, hasher)
     * 4. Update user's password in database
     * 5. User's security upgraded transparently (e.g., BCrypt 10→12 rounds)
     * 
     * Implementation:
     * Delegates to hasher which checks if hash uses outdated parameters
     * (e.g., old cost factor, deprecated algorithm variant)
     * 
     * @param hasher the password hasher to check current parameters against
     * @return true if password should be rehashed with current security standard
     */
    public boolean needsRehash(PasswordHasher hasher) {
        if (hasher == null) {
            return false;
        }
        return hasher.needsRehash(hashedValue);
    }

    /**
     * Gets the hashed password value (safe to store in database).
     * 
     * Format: Algorithm-specific (e.g., BCrypt: $2a$12$..., Argon2: $argon2id$...)
     * Contains: algorithm identifier, cost/parameters, salt, hash
     * 
     * @return the complete hashed password string
     */
    public String getHashedValue() {
        return hashedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedValue, password.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }

    @Override
    public String toString() {
        // SECURITY: Never expose hashed password in logs
        return "Password{***}";
    }
}
