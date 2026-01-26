package com.mustapha.ecommerce.user.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a valid email address.
 * Enforces email format validation and normalization.
 * 
 * Business Rules:
 * - Email format follows RFC 5322 simplified pattern
 * - Maximum length: 255 characters (database standard)
 * - Normalized to lowercase for consistency
 * - No consecutive dots allowed
 * - No leading/trailing dots in local part
 * - TLD length: 2-24 characters (supports modern TLDs like .technology)
 * 
 * Immutability: Once created, email cannot be changed (value object)
 */
public class Email {
    // RFC 5322 simplified pattern with modern TLD support
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,24}$"
    );
    
    private static final Pattern CONSECUTIVE_DOTS = Pattern.compile("\\.\\.");
    
    private static final int MAX_LENGTH = 255;
    private static final int MAX_LOCAL_PART = 64;  // RFC 5321 standard

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    /**
     * Creates an Email from a string, validating format.
     * 
     * @param value The email string to validate
     * @return Valid Email value object
     * @throws IllegalArgumentException if email is invalid
     */
    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        String trimmed = value.trim();
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Email cannot exceed " + MAX_LENGTH + " characters");
        }

        // Normalize to lowercase for consistency
        String normalized = trimmed.toLowerCase();

        // Validate no consecutive dots
        if (CONSECUTIVE_DOTS.matcher(normalized).find()) {
            throw new IllegalArgumentException("Email cannot contain consecutive dots: " + value);
        }

        // Validate local part constraints
        int atIndex = normalized.indexOf('@');
        if (atIndex > 0) {
            String localPart = normalized.substring(0, atIndex);
            
            if (localPart.length() > MAX_LOCAL_PART) {
                throw new IllegalArgumentException("Email local part cannot exceed " + MAX_LOCAL_PART + " characters");
            }
            
            if (localPart.startsWith(".") || localPart.endsWith(".")) {
                throw new IllegalArgumentException("Email local part cannot start or end with a dot: " + value);
            }
        }

        // Validate overall format
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }

        return new Email(normalized);
    }

    public String getValue() {
        return value;
    }

    /**
     * Gets the domain part of the email (e.g., "example.com" from "user@example.com").
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(atIndex + 1) : "";
    }

    /**
     * Gets the local part of the email (e.g., "user" from "user@example.com").
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(0, atIndex) : value;
    }

    /**
     * Checks if this email belongs to a specific domain.
     * Useful for business rules like "only company emails allowed".
     * 
     * @param domain Domain to check (e.g., "example.com")
     * @return true if email is from this domain
     */
    public boolean isFromDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        return getDomain().equalsIgnoreCase(domain.trim());
    }

    /**
     * Checks if this email is from any of the specified domains.
     * Useful for whitelisting multiple corporate domains.
     * 
     * @param domains Varargs of domains to check
     * @return true if email matches any domain
     */
    public boolean isFromAnyDomain(String... domains) {
        if (domains == null || domains.length == 0) {
            return false;
        }
        
        String emailDomain = getDomain();
        for (String domain : domains) {
            if (domain != null && emailDomain.equalsIgnoreCase(domain.trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
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
