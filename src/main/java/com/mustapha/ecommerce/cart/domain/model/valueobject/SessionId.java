package com.mustapha.ecommerce.cart.domain.model.valueobject;

import java.util.Objects;

/**
 * Session ID Value Object
 * 
 * Responsibility: Type-safe session identifier for anonymous carts
 * 
 * Pattern: Value Object
 * - Immutable (final class, final field)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Business Rules:
 * - Session ID cannot be null or blank
 * - Length between 1-255 characters
 * - Used to track anonymous user carts before login
 * 
 * Benefits:
 * - Prevents mixing session IDs with user IDs or cart IDs (compile-time safety)
 * - Encapsulates validation logic
 * - Clear domain language: SessionId vs String
 */
public final class SessionId {
    
    private static final int MAX_LENGTH = 255;
    
    private final String value;
    
    /**
     * Create a SessionId with validation
     * 
     * @param value The session identifier
     * @throws IllegalArgumentException if value is null, blank, or invalid length
     */
    public SessionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Session ID cannot exceed " + MAX_LENGTH + " characters");
        }
        
        this.value = value;
    }
    
    /**
     * Get the raw value (for persistence layer)
     */
    public String getValue() {
        return value;
    }
    
    // ========== Value Object Equality ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "SessionId(" + value + ")";
    }
}
