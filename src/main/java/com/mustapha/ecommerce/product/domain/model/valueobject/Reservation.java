package com.mustapha.ecommerce.product.domain.model.valueobject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reservation Value Object
 * Tracks stock reservation for a specific order
 * Pattern: Value Object (DDD)
 * 
 * Invariants:
 * - OrderId cannot be null or empty
 * - Quantity must be positive
 * - ReservedAt timestamp required
 */
public class Reservation {
    private final String orderId;
    private final int quantity;
    private final LocalDateTime reservedAt;

    private Reservation(String orderId, int quantity, LocalDateTime reservedAt) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.reservedAt = reservedAt;
    }

    public static Reservation of(String orderId, int quantity) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId cannot be null or empty");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        
        return new Reservation(orderId, quantity, LocalDateTime.now());
    }
    
    /**
     * Reconstitute reservation from persistence (e.g., database)
     * Use this when loading existing reservations with known timestamp
     */
    public static Reservation of(String orderId, int quantity, LocalDateTime reservedAt) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId cannot be null or empty");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        
        if (reservedAt == null) {
            throw new IllegalArgumentException("ReservedAt timestamp cannot be null");
        }
        
        return new Reservation(orderId, quantity, reservedAt);
    }

    public String getOrderId() {
        return orderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return quantity == that.quantity && 
               Objects.equals(orderId, that.orderId) && 
               Objects.equals(reservedAt, that.reservedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, quantity, reservedAt);
    }

    @Override
    public String toString() {
        return "Reservation{orderId='" + orderId + "', quantity=" + quantity + 
               ", reservedAt=" + reservedAt + "}";
    }
}
