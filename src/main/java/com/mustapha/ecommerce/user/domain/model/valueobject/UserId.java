package com.mustapha.ecommerce.user.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique user identifier.
 * Ensures type safety and prevents mixing user IDs with other entity IDs.
 */
public class UserId {
    private final UUID value;

    private UserId(UUID value) {
        this.value = Objects.requireNonNull(value, "User ID cannot be null");
    }

    /**
     * Creates a new random UserId.
     */
    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Creates a UserId from an existing UUID.
     */
    public static UserId of(UUID value) {
        return new UserId(value);
    }

    /**
     * Creates a UserId from a string representation.
     */
    public static UserId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User ID string cannot be null or blank");
        }
        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + value, e);
        }
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
