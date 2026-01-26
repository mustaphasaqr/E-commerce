package com.mustapha.ecommerce.user.auth.domain.model.valueobject;

import com.mustapha.ecommerce.user.auth.domain.exception.InvalidTokenException;

import java.util.Objects;

/**
 * Credentials Value Object (Auth Subdomain)
 * Represents login credentials (username/email + password).
 * Immutable and validated at creation.
 */
public class Credentials {
    private final String identifier; // email or username
    private final String plainPassword;

    private Credentials(String identifier, String plainPassword) {
        this.identifier = identifier;
        this.plainPassword = plainPassword;
    }

    /**
     * Factory: Create credentials from login input
     */
    public static Credentials of(String identifier, String plainPassword) {
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidTokenException("Identifier (email/username) cannot be null or blank");
        }
        
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new InvalidTokenException("Password cannot be null or blank");
        }
        
        return new Credentials(identifier.trim().toLowerCase(), plainPassword);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credentials that = (Credentials) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "identifier='" + identifier + '\'' +
                ", plainPassword='***'" + // Never log password
                '}';
    }
}
