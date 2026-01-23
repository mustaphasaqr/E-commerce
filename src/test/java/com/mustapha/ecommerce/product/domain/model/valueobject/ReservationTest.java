package com.mustapha.ecommerce.product.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Reservation Value Object Tests
 * Tests: Creation, validation, reconstitution, equality
 */
class ReservationTest {

    @Test
    void shouldCreateValidReservation() {
        Reservation reservation = Reservation.of("ORDER-123", 5);
        
        assertNotNull(reservation);
        assertEquals("ORDER-123", reservation.getOrderId());
        assertEquals(5, reservation.getQuantity());
        assertNotNull(reservation.getReservedAt());
    }

    @Test
    void shouldSetTimestampWhenCreated() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Reservation reservation = Reservation.of("ORDER-123", 5);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(reservation.getReservedAt().isAfter(before));
        assertTrue(reservation.getReservedAt().isBefore(after));
    }

    @Test
    void shouldReconstituteWithKnownTimestamp() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
        Reservation reservation = Reservation.of("ORDER-123", 5, timestamp);
        
        assertEquals("ORDER-123", reservation.getOrderId());
        assertEquals(5, reservation.getQuantity());
        assertEquals(timestamp, reservation.getReservedAt());
    }

    @Test
    void shouldRejectNullOrderId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of(null, 5);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectEmptyOrderId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of("", 5);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectBlankOrderId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of("   ", 5);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectZeroQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of("ORDER-123", 0);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of("ORDER-123", -5);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRejectNullTimestampInReconstitution() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of("ORDER-123", 5, null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldRejectNullOrderIdInReconstitution() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of(null, 5, LocalDateTime.now());
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectZeroQuantityInReconstitution() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Reservation.of("ORDER-123", 0, LocalDateTime.now());
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldBeEqualWhenAllFieldsMatch() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
        Reservation res1 = Reservation.of("ORDER-123", 5, timestamp);
        Reservation res2 = Reservation.of("ORDER-123", 5, timestamp);
        
        assertEquals(res1, res2);
        assertEquals(res1.hashCode(), res2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenOrderIdDiffers() {
        LocalDateTime timestamp = LocalDateTime.now();
        Reservation res1 = Reservation.of("ORDER-123", 5, timestamp);
        Reservation res2 = Reservation.of("ORDER-456", 5, timestamp);
        
        assertNotEquals(res1, res2);
    }

    @Test
    void shouldNotBeEqualWhenQuantityDiffers() {
        LocalDateTime timestamp = LocalDateTime.now();
        Reservation res1 = Reservation.of("ORDER-123", 5, timestamp);
        Reservation res2 = Reservation.of("ORDER-123", 10, timestamp);
        
        assertNotEquals(res1, res2);
    }

    @Test
    void shouldNotBeEqualWhenTimestampDiffers() {
        Reservation res1 = Reservation.of("ORDER-123", 5, LocalDateTime.of(2024, 1, 15, 10, 30));
        Reservation res2 = Reservation.of("ORDER-123", 5, LocalDateTime.of(2024, 1, 15, 11, 30));
        
        assertNotEquals(res1, res2);
    }

    @Test
    void shouldBeEqualToItself() {
        Reservation reservation = Reservation.of("ORDER-123", 5);
        
        assertEquals(reservation, reservation);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Reservation reservation = Reservation.of("ORDER-123", 5);
        
        assertNotEquals(null, reservation);
    }

    @Test
    void shouldFormatToString() {
        Reservation reservation = Reservation.of("ORDER-123", 5);
        
        String str = reservation.toString();
        assertTrue(str.contains("ORDER-123"));
        assertTrue(str.contains("5"));
    }

    @Test
    void shouldAcceptLargeQuantity() {
        Reservation reservation = Reservation.of("ORDER-123", 10000);
        
        assertEquals(10000, reservation.getQuantity());
    }
}
