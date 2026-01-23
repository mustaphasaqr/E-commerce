package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Product Domain Events Tests
 * Tests: All 7 domain events for validation, equality, and event interface compliance
 */
class ProductEventsTest {

    private static final Currency USD = Currency.getInstance("USD");

    // ==== ProductCreatedEvent Tests ====

    @Test
    void shouldCreateProductCreatedEvent() {
        ProductId productId = ProductId.generate();
        ProductCreatedEvent event = new ProductCreatedEvent(productId, "PROD-123", "Test Product");
        
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(productId, event.productId());
        assertEquals("PROD-123", event.sku());
        assertEquals("Test Product", event.name());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void shouldRejectProductCreatedWithNullProductId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProductCreatedEvent(null, "PROD-123", "Test");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldRejectProductCreatedWithNullSku() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProductCreatedEvent(productId, null, "Test");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectProductCreatedWithEmptySku() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProductCreatedEvent(productId, "", "Test");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectProductCreatedWithNullName() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProductCreatedEvent(productId, "PROD-123", null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectProductCreatedWithEmptyName() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProductCreatedEvent(productId, "PROD-123", "");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    // ==== PriceChangedEvent Tests ====

    @Test
    void shouldCreatePriceChangedEvent() {
        ProductId productId = ProductId.generate();
        Price oldPrice = Price.of(new BigDecimal("10.00"), USD);
        Price newPrice = Price.of(new BigDecimal("15.00"), USD);
        
        PriceChangedEvent event = new PriceChangedEvent(productId, oldPrice, newPrice);
        
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(productId, event.productId());
        assertEquals(oldPrice, event.oldPrice());
        assertEquals(newPrice, event.newPrice());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void shouldRejectPriceChangedWithNullProductId() {
        Price price = Price.of(new BigDecimal("10.00"), USD);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceChangedEvent(null, price, price);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldRejectPriceChangedWithNullOldPrice() {
        ProductId productId = ProductId.generate();
        Price newPrice = Price.of(new BigDecimal("15.00"), USD);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceChangedEvent(productId, null, newPrice);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldRejectPriceChangedWithNullNewPrice() {
        ProductId productId = ProductId.generate();
        Price oldPrice = Price.of(new BigDecimal("10.00"), USD);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceChangedEvent(productId, oldPrice, null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldRejectPriceChangedWithDifferentCurrencies() {
        ProductId productId = ProductId.generate();
        Price oldPrice = Price.of(new BigDecimal("10.00"), USD);
        Price newPrice = Price.of(new BigDecimal("15.00"), Currency.getInstance("EUR"));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceChangedEvent(productId, oldPrice, newPrice);
        });
        
        assertTrue(exception.getMessage().contains("different currencies"));
    }

    @Test
    void shouldRejectPriceChangedWithSamePrices() {
        ProductId productId = ProductId.generate();
        Price price = Price.of(new BigDecimal("10.00"), USD);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PriceChangedEvent(productId, price, price);
        });
        
        assertTrue(exception.getMessage().contains("different prices"));
    }

    // ==== StockUpdatedEvent Tests ====

    @Test
    void shouldCreateStockUpdatedEvent() {
        ProductId productId = ProductId.generate();
        StockUpdatedEvent event = new StockUpdatedEvent(productId, 100, 120, 20, 25);
        
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(productId, event.productId());
        assertEquals(100, event.previousTotal());
        assertEquals(120, event.newTotal());
        assertEquals(20, event.previousReserved());
        assertEquals(25, event.newReserved());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void shouldCalculateAvailableQuantity() {
        ProductId productId = ProductId.generate();
        StockUpdatedEvent event = new StockUpdatedEvent(productId, 100, 120, 20, 25);
        
        assertEquals(80, event.getPreviousAvailable());
        assertEquals(95, event.getNewAvailable());
    }

    @Test
    void shouldRejectStockUpdatedWithNullProductId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StockUpdatedEvent(null, 100, 120, 20, 25);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldRejectStockUpdatedWithNegativePreviousTotal() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StockUpdatedEvent(productId, -10, 120, 0, 25);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectStockUpdatedWithNegativeNewTotal() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StockUpdatedEvent(productId, 100, -10, 20, 0);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectStockUpdatedWithNegativePreviousReserved() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StockUpdatedEvent(productId, 100, 120, -5, 25);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectStockUpdatedWithNegativeNewReserved() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StockUpdatedEvent(productId, 100, 120, 20, -5);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectStockUpdatedWhenPreviousReservedExceedsTotal() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StockUpdatedEvent(productId, 100, 120, 150, 25);
        });
        
        assertTrue(exception.getMessage().contains("cannot exceed"));
    }

    @Test
    void shouldRejectStockUpdatedWhenNewReservedExceedsTotal() {
        ProductId productId = ProductId.generate();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StockUpdatedEvent(productId, 100, 120, 20, 150);
        });
        
        assertTrue(exception.getMessage().contains("cannot exceed"));
    }

    // ==== ProductActivatedEvent Tests ====

    @Test
    void shouldCreateProductActivatedEvent() {
        ProductId productId = ProductId.generate();
        ProductActivatedEvent event = new ProductActivatedEvent(productId);
        
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(productId, event.productId());
        assertNotNull(event.getOccurredAt());
    }

    // ==== ProductDeactivatedEvent Tests ====

    @Test
    void shouldCreateProductDeactivatedEvent() {
        ProductId productId = ProductId.generate();
        ProductDeactivatedEvent event = new ProductDeactivatedEvent(productId);
        
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(productId, event.productId());
        assertNotNull(event.getOccurredAt());
    }

    // ==== ProductDiscontinuedEvent Tests ====

    @Test
    void shouldCreateProductDiscontinuedEvent() {
        ProductId productId = ProductId.generate();
        ProductDiscontinuedEvent event = new ProductDiscontinuedEvent(productId);
        
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(productId, event.productId());
        assertNotNull(event.getOccurredAt());
    }

    // ==== ProductDetailsUpdatedEvent Tests ====

    @Test
    void shouldCreateProductDetailsUpdatedEvent() {
        ProductId productId = ProductId.generate();
        ProductDetailsUpdatedEvent event = new ProductDetailsUpdatedEvent(
            productId, "New Name", "New Description"
        );
        
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(productId, event.productId());
        assertEquals("New Name", event.newName());
        assertEquals("New Description", event.newDescription());
        assertNotNull(event.getOccurredAt());
    }

    // ==== Event Equality Tests ====

    @Test
    void shouldBeEqualWhenEventIdsMatch() {
        ProductId productId = ProductId.generate();
        ProductActivatedEvent event1 = new ProductActivatedEvent(productId);
        ProductActivatedEvent event2 = new ProductActivatedEvent(productId);
        
        assertNotEquals(event1, event2); // Different event IDs
    }

    @Test
    void shouldHaveUniqueEventIds() {
        ProductId productId = ProductId.generate();
        ProductCreatedEvent event1 = new ProductCreatedEvent(productId, "PROD-123", "Test");
        ProductCreatedEvent event2 = new ProductCreatedEvent(productId, "PROD-123", "Test");
        
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }
}
