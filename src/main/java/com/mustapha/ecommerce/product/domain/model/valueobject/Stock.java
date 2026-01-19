package com.mustapha.ecommerce.product.domain.model.valueobject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stock Value Object with Reservation Tracking
 * Responsibility: Product stock/inventory with reserved quantity management
 * Pattern: Value Object (DDD)
 * 
 * ORDER-BASED RESERVATION API:
 * All reservations must be tracked by OrderId for:
 * - Idempotency (duplicate reserve calls return unchanged)
 * - Traceability (know which order reserved what)
 * - Order lifecycle support (cancel, timeout, fulfill)
 * 
 * Methods:
 *    - reserveForOrder(orderId, qty)   → Creates reservation, updates Map + reservedQuantity
 *    - releaseReservationForOrder(id)  → Cancels reservation (e.g., order cancelled)
 *    - fulfillReservationForOrder(id)  → Completes reservation (e.g., order shipped)
 * 
 * For bulk operations (admin stock adjustments):
 *    - Use restock(amount) to add inventory
 *    - Use adjustTo(newTotal) to set absolute stock level
 * 
 * Invariants:
 * - Total quantity cannot be negative
 * - Reserved quantity cannot be negative
 * - Reserved quantity cannot exceed total quantity
 * - Available = Total - Reserved (always >= 0)
 * - Each OrderId can only reserve once (idempotency via Map)
 * - Reservation Map total must equal reservedQuantity (consistency)
 */
public class Stock {
    private final int totalQuantity;
    private final int reservedQuantity;
    private final Map<String, Reservation> reservationsByOrderId;

    private Stock(int totalQuantity, int reservedQuantity, Map<String, Reservation> reservationsByOrderId) {
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.reservationsByOrderId = new HashMap<>(reservationsByOrderId);
    }

    public static Stock of(int totalQuantity) {
        return of(totalQuantity, 0);
    }

    public static Stock of(int totalQuantity, int reservedQuantity) {
        return of(totalQuantity, reservedQuantity, Collections.emptyMap());
    }
    
    public static Stock of(int totalQuantity, int reservedQuantity, Map<String, Reservation> reservations) {
        // Guard: Total cannot be negative
        if (totalQuantity < 0) {
            throw new IllegalArgumentException("Total stock quantity cannot be negative");
        }
        
        // Guard: Reserved cannot be negative
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("Reserved stock quantity cannot be negative");
        }
        
        // Guard: Reserved cannot exceed total
        if (reservedQuantity > totalQuantity) {
            throw new IllegalArgumentException(
                "Reserved quantity (" + reservedQuantity + ") cannot exceed total quantity (" + totalQuantity + ")"
            );
        }
        
        // Guard: Reservation Map must be consistent with reservedQuantity
        if (reservations != null && !reservations.isEmpty()) {
            int mapTotal = reservations.values().stream()
                .mapToInt(Reservation::getQuantity)
                .sum();
            if (mapTotal != reservedQuantity) {
                throw new IllegalArgumentException(
                    "Reservation map total (" + mapTotal + ") does not match reserved quantity (" + reservedQuantity + ")"
                );
            }
        }
        
        return new Stock(totalQuantity, reservedQuantity, reservations);
    }

    public static Stock empty() {
        return new Stock(0, 0, Collections.emptyMap());
    }

    /**
     * Get total quantity (including reserved)
     */
    public int getQuantity() {
        return totalQuantity;
    }

    /**
     * Get reserved quantity
     */
    public int getReservedQuantity() {
        return reservedQuantity;
    }
    
    /**
     * Get available quantity (total - reserved)
     */
    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }
    
    /**
     * Get all reservations (immutable map)
     */
    public Map<String, Reservation> getReservations() {
        return Collections.unmodifiableMap(reservationsByOrderId);
    }
    
    /**
     * Check if order has existing reservation
     */
    public boolean hasReservation(String orderId) {
        return reservationsByOrderId.containsKey(orderId);
    }
    
    /**
     * Get reservation for specific order
     */
    public Reservation getReservation(String orderId) {
        return reservationsByOrderId.get(orderId);
    }
    
    /**
     * Check if there's enough available stock for requested quantity
     */
    public boolean hasQuantity(int amount) {
        return getAvailableQuantity() >= amount;
    }

    /**
     * Reserve stock for specific order (decreases available, not total)
     * 
     * Business Rules:
     * - Can only reserve from available stock
     * - Idempotent per OrderId (returns unchanged if already reserved)
     * - Tracks reservation in Map for order lifecycle management
     * - Amount must be positive (> 0)
     * 
     * Use this for: Order checkout, cart conversion, order workflows
     */
    public Stock reserveForOrder(String orderId, int amount) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId cannot be null or empty");
        }
        
        // Idempotency: If order already has reservation, return unchanged
        if (reservationsByOrderId.containsKey(orderId)) {
            return this; // Idempotent - already reserved
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Reservation amount must be positive");
        }
        
        int available = getAvailableQuantity();
        if (amount > available) {
            throw new IllegalArgumentException(
                "Insufficient available stock. Available: " + available + 
                ", Requested: " + amount + " (Total: " + totalQuantity + 
                ", Reserved: " + reservedQuantity + ")"
            );
        }
        
        Map<String, Reservation> newReservations = new HashMap<>(reservationsByOrderId);
        newReservations.put(orderId, Reservation.of(orderId, amount));
        
        return new Stock(totalQuantity, reservedQuantity + amount, newReservations);
    }
    
    /**
     * Release reservation for specific order (e.g., order cancelled)
     * 
     * Business Rule: Idempotent (returns unchanged if no reservation exists)
     * 
     * Use this for: Order cancellation, payment failure, timeout workflows
     */
    public Stock releaseReservationForOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId cannot be null or empty");
        }
        
        Reservation reservation = reservationsByOrderId.get(orderId);
        if (reservation == null) {
            return this; // Idempotent - no reservation to release
        }
        
        Map<String, Reservation> newReservations = new HashMap<>(reservationsByOrderId);
        newReservations.remove(orderId);
        
        return new Stock(totalQuantity, reservedQuantity - reservation.getQuantity(), newReservations);
    }
    
    /**
     * Fulfill reservation for specific order (decreases both reserved and total)
     * 
     * Business Rule: Removes reservation from Map and decrements both counters
     * 
     * Use this for: Order shipment, order completion workflows
     * Called when order is shipped/completed
     */
    public Stock fulfillReservationForOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId cannot be null or empty");
        }
        
        Reservation reservation = reservationsByOrderId.get(orderId);
        if (reservation == null) {
            throw new IllegalArgumentException("No reservation found for order: " + orderId);
        }
        
        Map<String, Reservation> newReservations = new HashMap<>(reservationsByOrderId);
        newReservations.remove(orderId);
        
        int amount = reservation.getQuantity();
        return new Stock(totalQuantity - amount, reservedQuantity - amount, newReservations);
    }

    /**
     * Add stock (increases total, reserved stays same)
     * Use this for: Receiving inventory shipments, admin stock increases
     */
    public Stock restock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restock amount must be positive");
        }
        
        return new Stock(totalQuantity + amount, reservedQuantity, reservationsByOrderId);
    }

    /**
     * Adjust stock to specific total (reserved stays same if possible)
     * Use this for: Admin corrections, inventory audits
     */
    public Stock adjustTo(int newTotal) {
        if (newTotal < 0) {
            throw new IllegalArgumentException("New total cannot be negative");
        }
        
        if (newTotal < reservedQuantity) {
            throw new IllegalArgumentException(
                "Cannot set total (" + newTotal + ") below reserved (" + reservedQuantity + ")"
            );
        }
        
        return new Stock(newTotal, reservedQuantity, reservationsByOrderId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return totalQuantity == stock.totalQuantity && 
               reservedQuantity == stock.reservedQuantity &&
               Objects.equals(reservationsByOrderId, stock.reservationsByOrderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalQuantity, reservedQuantity, reservationsByOrderId);
    }

    @Override
    public String toString() {
        return "Stock{total=" + totalQuantity + ", reserved=" + reservedQuantity + 
               ", available=" + getAvailableQuantity() + ", reservations=" + reservationsByOrderId.size() + "}";
    }
}
