package com.mustapha.ecommerce.user.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a username.
 * Enforces validation rules to prevent SQL injection, XSS, and ensure usability.
 * 
 * Business Rules:
 * - Length: 3-30 characters
 * - Characters: Letters, numbers, underscore, hyphen only
 * - Must start and end with alphanumeric character (not special char)
 * - No consecutive special characters (__, --, _-, -_)
 * - Case-insensitive (stored as lowercase for uniqueness)
 * - Reserved usernames blocked (admin, system, api, etc.)
 * 
 * Security: Prevents SQL injection, XSS, and username enumeration attacks
 */
public class Username {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 30;
    
    // Must start/end with alphanumeric, middle can have single _ or -
    // Prevents: -test, test-, test--name, user__123
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]([a-zA-Z0-9]|[_-](?=[a-zA-Z0-9]))*[a-zA-Z0-9]$|^[a-zA-Z0-9]$"
    );
    
    // Reserved usernames (lowercase) - prevent impersonation and system conflicts
    private static final java.util.Set<String> RESERVED_USERNAMES = java.util.Set.of(
        // System accounts
        "admin", "administrator", "system", "root", "superuser",
        // Support/moderation
        "support", "moderator", "mod",
        // Role-based (matches Role enum to prevent confusion)
        "owner", "employee", "customer",
        // API/Technical (prevent routing conflicts)
        "api", "www", "ftp", "mail", "smtp", "localhost",
        // Generic/Test (prevent ambiguity)
        "test", "guest", "user", "null", "undefined", "anonymous"
    );

    private final String value;

    private Username(String value) {
        this.value = value;
    }

    /**
     * Creates a Username from a string, validating format.
     * Normalizes to lowercase for case-insensitive uniqueness.
     */
    public static Username of(String value) {
        String normalized = validateAndNormalize(value);
        return new Username(normalized);
    }

    /**
     * Public validation method for username.
     * Useful for UI/API validation before attempting to create Username object.
     * 
     * @param value Username to validate
     * @throws IllegalArgumentException with specific error message if validation fails
     */
    public static void validate(String value) {
        validateAndNormalize(value);
    }

    /**
     * Validates and normalizes username to lowercase.
     */
    private static String validateAndNormalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }

        String trimmed = value.trim();

        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "Username must be at least " + MIN_LENGTH + " characters long"
            );
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Username cannot exceed " + MAX_LENGTH + " characters"
            );
        }

        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "Username must start and end with a letter or number, and can only contain letters, numbers, single underscores, and single hyphens"
            );
        }

        // Normalize to lowercase for case-insensitive uniqueness
        String normalized = trimmed.toLowerCase();

        // Prevent reserved usernames
        if (isReserved(normalized) && !isTemporarilyAllowedReservedUsername(normalized)) {
            throw new IllegalArgumentException("Username '" + trimmed + "' is reserved");
        }

        return normalized;
    }

    /**
     * Checks if username is reserved (system, API, role names, etc.).
     * Case-insensitive check on normalized lowercase username.
     */
    private static boolean isReserved(String normalizedUsername) {
        return RESERVED_USERNAMES.contains(normalizedUsername);
    }

    /**
     * Temporary escape hatch for one-time owner signup.
     * Controlled by either:
     * - JVM property: app.owner.one-time-signup.enabled=true
     * - Env var: APP_OWNER_ONE_TIME_SIGNUP_ENABLED=true
     */
    private static boolean isTemporarilyAllowedReservedUsername(String normalizedUsername) {
        if (!"owner".equals(normalizedUsername)) {
            return false;
        }

        String propertyValue = System.getProperty("app.owner.one-time-signup.enabled");
        String legacyPropertyValue = System.getProperty("OWNER_ONE_TIME_SIGNUP_ENABLED");
        String envValue = System.getenv("APP_OWNER_ONE_TIME_SIGNUP_ENABLED");
        String springStyleEnvValue = System.getenv("OWNER_ONE_TIME_SIGNUP_ENABLED");

        return "true".equalsIgnoreCase(propertyValue)
            || "true".equalsIgnoreCase(legacyPropertyValue)
            || "true".equalsIgnoreCase(envValue)
            || "true".equalsIgnoreCase(springStyleEnvValue);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return Objects.equals(value, username.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
