package com.mustapha.ecommerce.product.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stock Value Object Tests
 * Tests: Creation, reservations, fulfillment, restocking, invariants
 */
class StockTest {

    @Test
    void shouldCreateStockWithTotalQuantity() {
        Stock stock = Stock.of(100);
        
        assertEquals(100, stock.getQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(100, stock.getAvailableQuantity());
    }

    @Test
    void shouldCreateStockWithReservedQuantity() {
        Stock stock = Stock.of(100, 20);
        
        assertEquals(100, stock.getQuantity());
        assertEquals(20, stock.getReservedQuantity());
        assertEquals(80, stock.getAvailableQuantity());
    }

    @Test
    void shouldCreateEmptyStock() {
        Stock stock = Stock.empty();
        
        assertEquals(0, stock.getQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(0, stock.getAvailableQuantity());
    }

    @Test
    void shouldCreateStockWithReservationsMap() {
        Map<String, Reservation> reservations = new HashMap<>();
        reservations.put("ORDER-1", Reservation.of("ORDER-1", 10));
        reservations.put("ORDER-2", Reservation.of("ORDER-2", 5));
        
        Stock stock = Stock.of(100, 15, reservations);
        
        assertEquals(100, stock.getQuantity());
        assertEquals(15, stock.getReservedQuantity());
        assertEquals(85, stock.getAvailableQuantity());
        assertTrue(stock.hasReservation("ORDER-1"));
        assertTrue(stock.hasReservation("ORDER-2"));
    }

    @Test
    void shouldRejectNegativeTotalQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Stock.of(-10);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectNegativeReservedQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Stock.of(100, -10);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectReservedExceedingTotal() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Stock.of(100, 150);
        });
        
        assertTrue(exception.getMessage().contains("cannot exceed total"));
    }

    @Test
    void shouldRejectInconsistentReservationMap() {
        Map<String, Reservation> reservations = new HashMap<>();
        reservations.put("ORDER-1", Reservation.of("ORDER-1", 10));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Stock.of(100, 20, reservations); // Map has 10, but reserved is 20
        });
        
        assertTrue(exception.getMessage().contains("does not match"));
    }

    @Test
    void shouldCheckIfHasQuantity() {
        Stock stock = Stock.of(100, 20);
        
        assertTrue(stock.hasQuantity(80));
        assertTrue(stock.hasQuantity(50));
        assertFalse(stock.hasQuantity(81));
        assertFalse(stock.hasQuantity(100));
    }

    @Test
    void shouldReserveStockForOrder() {
        Stock stock = Stock.of(100);
        Stock updated = stock.reserveForOrder("ORDER-123", 20);
        
        assertEquals(100, updated.getQuantity());
        assertEquals(20, updated.getReservedQuantity());
        assertEquals(80, updated.getAvailableQuantity());
        assertTrue(updated.hasReservation("ORDER-123"));
    }

    @Test
    void shouldBeIdempotentWhenReservingTwice() {
        Stock stock = Stock.of(100);
        Stock first = stock.reserveForOrder("ORDER-123", 20);
        Stock second = first.reserveForOrder("ORDER-123", 30);
        
        assertSame(first, second);
        assertEquals(20, second.getReservedQuantity());
    }

    @Test
    void shouldRejectReservationWithNullOrderId() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.reserveForOrder(null, 20);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectReservationWithEmptyOrderId() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.reserveForOrder("", 20);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectReservationWithZeroAmount() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.reserveForOrder("ORDER-123", 0);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRejectReservationWithNegativeAmount() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.reserveForOrder("ORDER-123", -10);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRejectReservationWhenInsufficientStock() {
        Stock stock = Stock.of(100, 20);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.reserveForOrder("ORDER-123", 90);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient available stock"));
        assertTrue(exception.getMessage().contains("Available: 80"));
        assertTrue(exception.getMessage().contains("Requested: 90"));
    }

    @Test
    void shouldReleaseReservationForOrder() {
        Stock stock = Stock.of(100).reserveForOrder("ORDER-123", 20);
        Stock updated = stock.releaseReservationForOrder("ORDER-123");
        
        assertEquals(100, updated.getQuantity());
        assertEquals(0, updated.getReservedQuantity());
        assertEquals(100, updated.getAvailableQuantity());
        assertFalse(updated.hasReservation("ORDER-123"));
    }

    @Test
    void shouldBeIdempotentWhenReleasingNonExistentReservation() {
        Stock stock = Stock.of(100);
        Stock updated = stock.releaseReservationForOrder("ORDER-123");
        
        assertSame(stock, updated);
    }

    @Test
    void shouldRejectReleaseWithNullOrderId() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.releaseReservationForOrder(null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldFulfillReservationForOrder() {
        Stock stock = Stock.of(100).reserveForOrder("ORDER-123", 20);
        Stock updated = stock.fulfillReservationForOrder("ORDER-123");
        
        assertEquals(80, updated.getQuantity());
        assertEquals(0, updated.getReservedQuantity());
        assertEquals(80, updated.getAvailableQuantity());
        assertFalse(updated.hasReservation("ORDER-123"));
    }

    @Test
    void shouldRejectFulfillmentWithoutReservation() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.fulfillReservationForOrder("ORDER-123");
        });
        
        assertTrue(exception.getMessage().contains("No reservation found"));
    }

    @Test
    void shouldRejectFulfillWithNullOrderId() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.fulfillReservationForOrder(null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRestockInventory() {
        Stock stock = Stock.of(100, 20);
        Stock updated = stock.restock(50);
        
        assertEquals(150, updated.getQuantity());
        assertEquals(20, updated.getReservedQuantity());
        assertEquals(130, updated.getAvailableQuantity());
    }

    @Test
    void shouldRejectRestockWithZeroAmount() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.restock(0);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRejectRestockWithNegativeAmount() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.restock(-10);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldAdjustTotalQuantity() {
        Stock stock = Stock.of(100, 20);
        Stock updated = stock.adjustTo(150);
        
        assertEquals(150, updated.getQuantity());
        assertEquals(20, updated.getReservedQuantity());
        assertEquals(130, updated.getAvailableQuantity());
    }

    @Test
    void shouldRejectAdjustToNegative() {
        Stock stock = Stock.of(100);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.adjustTo(-10);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectAdjustBelowReserved() {
        Stock stock = Stock.of(100, 20);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stock.adjustTo(15);
        });
        
        assertTrue(exception.getMessage().contains("below reserved"));
    }

    @Test
    void shouldGetReservation() {
        Stock stock = Stock.of(100).reserveForOrder("ORDER-123", 20);
        Reservation reservation = stock.getReservation("ORDER-123");
        
        assertNotNull(reservation);
        assertEquals("ORDER-123", reservation.getOrderId());
        assertEquals(20, reservation.getQuantity());
    }

    @Test
    void shouldReturnNullForNonExistentReservation() {
        Stock stock = Stock.of(100);
        
        assertNull(stock.getReservation("ORDER-999"));
    }

    @Test
    void shouldReturnImmutableReservationsMap() {
        Stock stock = Stock.of(100).reserveForOrder("ORDER-123", 20);
        Map<String, Reservation> reservations = stock.getReservations();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            reservations.put("ORDER-456", Reservation.of("ORDER-456", 10));
        });
    }

    @Test
    void shouldHandleMultipleReservations() {
        Stock stock = Stock.of(100)
            .reserveForOrder("ORDER-1", 20)
            .reserveForOrder("ORDER-2", 30)
            .reserveForOrder("ORDER-3", 10);
        
        assertEquals(100, stock.getQuantity());
        assertEquals(60, stock.getReservedQuantity());
        assertEquals(40, stock.getAvailableQuantity());
        assertEquals(3, stock.getReservations().size());
    }

    @Test
    void shouldBeEqualWhenAllFieldsMatch() {
        Stock stock1 = Stock.of(100, 20);
        Stock stock2 = Stock.of(100, 20);
        
        assertEquals(stock1, stock2);
        assertEquals(stock1.hashCode(), stock2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenTotalDiffers() {
        Stock stock1 = Stock.of(100, 20);
        Stock stock2 = Stock.of(150, 20);
        
        assertNotEquals(stock1, stock2);
    }

    @Test
    void shouldNotBeEqualWhenReservedDiffers() {
        Stock stock1 = Stock.of(100, 20);
        Stock stock2 = Stock.of(100, 30);
        
        assertNotEquals(stock1, stock2);
    }

    @Test
    void shouldBeEqualToItself() {
        Stock stock = Stock.of(100, 20);
        
        assertEquals(stock, stock);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Stock stock = Stock.of(100);
        
        assertNotEquals(null, stock);
    }

    @Test
    void shouldFormatToString() {
        Stock stock = Stock.of(100, 20);
        
        String str = stock.toString();
        assertTrue(str.contains("100"));
        assertTrue(str.contains("20"));
        assertTrue(str.contains("80"));
    }
}
