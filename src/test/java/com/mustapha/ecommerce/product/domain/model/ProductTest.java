package com.mustapha.ecommerce.product.domain.model;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.event.*;
import com.mustapha.ecommerce.product.domain.exception.*;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Product Aggregate Root Tests
 * Tests: Creation, state transitions, reservations, business rules, invariants
 */
class ProductTest {

    private static final Currency USD = Currency.getInstance("USD");
    private SKU validSku;
    private Price validPrice;
    private Stock validStock;

    @BeforeEach
    void setUp() {
        validSku = SKU.of("PROD-123");
        validPrice = Price.of(new BigDecimal("99.99"), USD);
        validStock = Stock.of(100);
    }

    // ==== Creation Tests ====

    @Test
    void shouldCreateProduct() {
        Product product = Product.create(validSku, "Test Product", "Description", validPrice, validStock);
        
        assertNotNull(product);
        assertNotNull(product.getId());
        assertEquals(validSku, product.getSku());
        assertEquals("Test Product", product.getName());
        assertEquals("Description", product.getDescription());
        assertEquals(validPrice, product.getPrice());
        assertEquals(validStock.getQuantity(), product.getStock().getQuantity());
        assertTrue(product.isActive());
        assertTrue(product.isVisible());
        assertTrue(product.isAvailableForPurchase());
        assertFalse(product.isDiscontinued());
        assertEquals(1, product.getVersion());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    void shouldEmitProductCreatedEvent() {
        Product product = Product.create(validSku, "Test Product", "Description", validPrice, validStock);
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductCreatedEvent);
        
        ProductCreatedEvent event = (ProductCreatedEvent) events.get(0);
        assertEquals(product.getId(), event.productId());
        assertEquals("PROD-123", event.sku());
        assertEquals("Test Product", event.name());
    }

    @Test
    void shouldRejectProductWithNullName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Product.create(validSku, null, "Description", validPrice, validStock);
        });
        
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void shouldRejectProductWithEmptyName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Product.create(validSku, "", "Description", validPrice, validStock);
        });
        
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void shouldRejectProductWithBlankName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Product.create(validSku, "   ", "Description", validPrice, validStock);
        });
        
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void shouldRejectProductNameExceeding200Characters() {
        String longName = "A".repeat(201);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Product.create(validSku, longName, "Description", validPrice, validStock);
        });
        
        assertTrue(exception.getMessage().contains("cannot exceed 200 characters"));
    }

    @Test
    void shouldAcceptProductNameWith200Characters() {
        String maxName = "A".repeat(200);
        
        Product product = Product.create(validSku, maxName, "Description", validPrice, validStock);
        
        assertEquals(maxName, product.getName());
    }

    @Test
    void shouldClearDomainEvents() {
        Product product = Product.create(validSku, "Test Product", "Description", validPrice, validStock);
        
        assertEquals(1, product.getDomainEvents().size());
        product.clearDomainEvents();
        assertEquals(0, product.getDomainEvents().size());
    }

    // ==== Stock Reservation Tests ====

    @Test
    void shouldReserveStockForOrder() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.reserveStockForOrder("ORDER-123", 20);
        
        assertEquals(100, product.getStock().getQuantity());
        assertEquals(20, product.getStock().getReservedQuantity());
        assertEquals(80, product.getStock().getAvailableQuantity());
        assertTrue(product.getStock().hasReservation("ORDER-123"));
        assertEquals(2, product.getVersion());
    }

    @Test
    void shouldEmitStockUpdatedEventWhenReserving() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.reserveStockForOrder("ORDER-123", 20);
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof StockUpdatedEvent);
        
        StockUpdatedEvent event = (StockUpdatedEvent) events.get(0);
        assertEquals(100, event.previousTotal());
        assertEquals(100, event.newTotal());
        assertEquals(0, event.previousReserved());
        assertEquals(20, event.newReserved());
    }

    @Test
    void shouldBeIdempotentWhenReservingTwiceForSameOrder() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.reserveStockForOrder("ORDER-123", 20);
        int versionAfterFirst = product.getVersion();
        
        product.reserveStockForOrder("ORDER-123", 30);
        
        assertEquals(versionAfterFirst, product.getVersion());
        assertEquals(20, product.getStock().getReservedQuantity());
    }

    @Test
    void shouldRejectReservationForInactiveProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.deactivate();
        
        Exception exception = assertThrows(InvalidProductStateException.class, () -> {
            product.reserveStockForOrder("ORDER-123", 20);
        });
        
        assertTrue(exception.getMessage().contains("inactive"));
    }

    @Test
    void shouldRejectReservationForDiscontinuedProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.discontinue();
        
        Exception exception = assertThrows(ProductDiscontinuedException.class, () -> {
            product.reserveStockForOrder("ORDER-123", 20);
        });
    }

    @Test
    void shouldRejectReservationWhenNotAvailableForPurchase() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.makeUnavailableForPurchase();
        
        Exception exception = assertThrows(InvalidProductStateException.class, () -> {
            product.reserveStockForOrder("ORDER-123", 20);
        });
        
        assertTrue(exception.getMessage().contains("not available for purchase"));
    }

    @Test
    void shouldRejectReservationWithZeroQuantity() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.reserveStockForOrder("ORDER-123", 0);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRejectReservationWithNegativeQuantity() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.reserveStockForOrder("ORDER-123", -5);
        });
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    // ==== Release Reservation Tests ====

    @Test
    void shouldReleaseReservation() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.reserveStockForOrder("ORDER-123", 20);
        product.clearDomainEvents();
        
        product.releaseReservationForOrder("ORDER-123");
        
        assertEquals(100, product.getStock().getQuantity());
        assertEquals(0, product.getStock().getReservedQuantity());
        assertFalse(product.getStock().hasReservation("ORDER-123"));
    }

    @Test
    void shouldEmitStockUpdatedEventWhenReleasing() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.reserveStockForOrder("ORDER-123", 20);
        product.clearDomainEvents();
        
        product.releaseReservationForOrder("ORDER-123");
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof StockUpdatedEvent);
        
        StockUpdatedEvent event = (StockUpdatedEvent) events.get(0);
        assertEquals(100, event.previousTotal());
        assertEquals(100, event.newTotal());
        assertEquals(20, event.previousReserved());
        assertEquals(0, event.newReserved());
    }

    @Test
    void shouldBeIdempotentWhenReleasingNonExistentReservation() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.releaseReservationForOrder("ORDER-999");
        
        assertEquals(0, product.getDomainEvents().size());
    }

    @Test
    void shouldRejectReleaseForDiscontinuedProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.reserveStockForOrder("ORDER-123", 20);
        product.discontinue();
        
        Exception exception = assertThrows(ProductDiscontinuedException.class, () -> {
            product.releaseReservationForOrder("ORDER-123");
        });
    }

    // ==== Fulfill Reservation Tests ====

    @Test
    void shouldFulfillReservation() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.reserveStockForOrder("ORDER-123", 20);
        product.clearDomainEvents();
        
        product.fulfillReservationForOrder("ORDER-123");
        
        assertEquals(80, product.getStock().getQuantity());
        assertEquals(0, product.getStock().getReservedQuantity());
        assertFalse(product.getStock().hasReservation("ORDER-123"));
    }

    @Test
    void shouldEmitStockUpdatedEventWhenFulfilling() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.reserveStockForOrder("ORDER-123", 20);
        product.clearDomainEvents();
        
        product.fulfillReservationForOrder("ORDER-123");
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof StockUpdatedEvent);
        
        StockUpdatedEvent event = (StockUpdatedEvent) events.get(0);
        assertEquals(100, event.previousTotal());
        assertEquals(80, event.newTotal());
        assertEquals(20, event.previousReserved());
        assertEquals(0, event.newReserved());
    }

    @Test
    void shouldRejectFulfillmentWithoutReservation() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.fulfillReservationForOrder("ORDER-999");
        });
        
        assertTrue(exception.getMessage().contains("No reservation found"));
    }

    @Test
    void shouldRejectFulfillForDiscontinuedProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.reserveStockForOrder("ORDER-123", 20);
        product.discontinue();
        
        Exception exception = assertThrows(ProductDiscontinuedException.class, () -> {
            product.fulfillReservationForOrder("ORDER-123");
        });
    }

    // ==== Price Update Tests ====

    @Test
    void shouldUpdatePrice() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        Price newPrice = Price.of(new BigDecimal("149.99"), USD);
        product.updatePrice(newPrice, false);
        
        assertEquals(newPrice, product.getPrice());
        assertEquals(2, product.getVersion());
    }

    @Test
    void shouldEmitPriceChangedEvent() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        Price newPrice = Price.of(new BigDecimal("149.99"), USD);
        product.updatePrice(newPrice, false);
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof PriceChangedEvent);
        
        PriceChangedEvent event = (PriceChangedEvent) events.get(0);
        assertEquals(validPrice, event.oldPrice());
        assertEquals(newPrice, event.newPrice());
    }

    @Test
    void shouldRejectPriceUpdateWhenHasActiveOrders() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        Price newPrice = Price.of(new BigDecimal("149.99"), USD);
        
        Exception exception = assertThrows(ProductInUseException.class, () -> {
            product.updatePrice(newPrice, true);
        });
        
        assertTrue(exception.getMessage().contains("active orders"));
    }

    @Test
    void shouldRejectPriceUpdateWithNullPrice() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.updatePrice(null, false);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldRejectPriceUpdateWithDifferentCurrency() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        Price eurPrice = Price.of(new BigDecimal("89.99"), Currency.getInstance("EUR"));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.updatePrice(eurPrice, false);
        });
        
        assertTrue(exception.getMessage().contains("different currencies"));
    }

    @Test
    void shouldSkipPriceUpdateWhenPriceUnchanged() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.updatePrice(validPrice, false);
        
        assertEquals(0, product.getDomainEvents().size());
    }

    @Test
    void shouldRejectPriceIncreaseExceeding10x() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        Price hugePrice = Price.of(new BigDecimal("1500.00"), USD);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.updatePrice(hugePrice, false);
        });
        
        assertTrue(exception.getMessage().contains("too drastic"));
    }

    @Test
    void shouldRejectPriceDecreaseExceeding90Percent() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        Price tinyPrice = Price.of(new BigDecimal("5.00"), USD);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            product.updatePrice(tinyPrice, false);
        });
        
        assertTrue(exception.getMessage().contains("too drastic"));
    }

    @Test
    void shouldRejectPriceUpdateForDiscontinuedProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.discontinue();
        
        Price newPrice = Price.of(new BigDecimal("149.99"), USD);
        
        Exception exception = assertThrows(ProductDiscontinuedException.class, () -> {
            product.updatePrice(newPrice, false);
        });
    }

    // ==== Details Update Tests ====

    @Test
    void shouldUpdateDetails() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.updateDetails("New Name", "New Description", false);
        
        assertEquals("New Name", product.getName());
        assertEquals("New Description", product.getDescription());
        assertEquals(2, product.getVersion());
    }

    @Test
    void shouldEmitDetailsUpdatedEvent() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.updateDetails("New Name", "New Description", false);
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductDetailsUpdatedEvent);
        
        ProductDetailsUpdatedEvent event = (ProductDetailsUpdatedEvent) events.get(0);
        assertEquals("New Name", event.newName());
        assertEquals("New Description", event.newDescription());
    }

    @Test
    void shouldRejectDetailsUpdateWhenHasActiveOrders() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        Exception exception = assertThrows(ProductInUseException.class, () -> {
            product.updateDetails("New Name", "New Description", true);
        });
        
        assertTrue(exception.getMessage().contains("active orders"));
    }

    @Test
    void shouldRejectDetailsUpdateForDiscontinuedProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.discontinue();
        
        Exception exception = assertThrows(ProductDiscontinuedException.class, () -> {
            product.updateDetails("New Name", "New Description", false);
        });
    }

    // ==== Deactivate Tests ====

    @Test
    void shouldDeactivateProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.deactivate();
        
        assertFalse(product.isActive());
        assertFalse(product.isAvailableForPurchase());
        assertEquals(2, product.getVersion());
    }

    @Test
    void shouldEmitProductDeactivatedEvent() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.deactivate();
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductDeactivatedEvent);
    }

    @Test
    void shouldRejectDeactivateWhenAlreadyInactive() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.deactivate();
        
        Exception exception = assertThrows(ProductAlreadyInactiveException.class, () -> {
            product.deactivate();
        });
    }

    @Test
    void shouldRejectDeactivateWhenHasReservedStock() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.reserveStockForOrder("ORDER-123", 20);
        
        Exception exception = assertThrows(InvalidProductStateException.class, () -> {
            product.deactivate();
        });
        
        assertTrue(exception.getMessage().contains("reserved"));
    }

    @Test
    void shouldRejectDeactivateForDiscontinuedProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.discontinue();
        
        Exception exception = assertThrows(ProductDiscontinuedException.class, () -> {
            product.deactivate();
        });
    }

    // ==== Activate Tests ====

    @Test
    void shouldActivateProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.deactivate();
        product.clearDomainEvents();
        
        product.activate();
        
        assertTrue(product.isActive());
        assertEquals(3, product.getVersion());
    }

    @Test
    void shouldEmitProductActivatedEvent() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.deactivate();
        product.clearDomainEvents();
        
        product.activate();
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductActivatedEvent);
    }

    @Test
    void shouldRejectActivateWhenAlreadyActive() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        Exception exception = assertThrows(ProductAlreadyActiveException.class, () -> {
            product.activate();
        });
    }

    @Test
    void shouldRejectActivateForDiscontinuedProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.discontinue();
        
        Exception exception = assertThrows(ProductDiscontinuedException.class, () -> {
            product.activate();
        });
    }

    // ==== Discontinue Tests ====

    @Test
    void shouldDiscontinueProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.discontinue();
        
        assertTrue(product.isDiscontinued());
        assertFalse(product.isActive());
        assertFalse(product.isAvailableForPurchase());
    }

    @Test
    void shouldEmitProductDiscontinuedEvent() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.clearDomainEvents();
        
        product.discontinue();
        
        List<ProductDomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductDiscontinuedEvent);
    }

    @Test
    void shouldBeIdempotentWhenDiscontinuingTwice() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.discontinue();
        product.clearDomainEvents();
        
        product.discontinue();
        
        assertEquals(0, product.getDomainEvents().size());
    }

    // ==== Visibility Tests ====

    @Test
    void shouldHideProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        product.hide();
        
        assertFalse(product.isVisible());
    }

    @Test
    void shouldShowProduct() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.hide();
        
        product.show();
        
        assertTrue(product.isVisible());
    }

    @Test
    void shouldBeIdempotentWhenHidingTwice() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.hide();
        int versionAfterFirst = product.getVersion();
        
        product.hide();
        
        assertEquals(versionAfterFirst, product.getVersion());
    }

    @Test
    void shouldBeIdempotentWhenShowingTwice() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        int versionAfterCreate = product.getVersion();
        
        product.show();
        
        assertEquals(versionAfterCreate, product.getVersion());
    }

    // ==== Purchase Availability Tests ====

    @Test
    void shouldMakeAvailableForPurchase() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.makeUnavailableForPurchase();
        
        product.makeAvailableForPurchase();
        
        assertTrue(product.isAvailableForPurchase());
    }

    @Test
    void shouldMakeUnavailableForPurchase() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        product.makeUnavailableForPurchase();
        
        assertFalse(product.isAvailableForPurchase());
    }

    // ==== Stock Availability Query Tests ====

    @Test
    void shouldCheckStockAvailability() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        
        assertTrue(product.isStockAvailable(50));
        assertTrue(product.isStockAvailable(100));
        assertFalse(product.isStockAvailable(101));
    }

    @Test
    void shouldReturnFalseWhenProductInactive() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.deactivate();
        
        assertFalse(product.isStockAvailable(50));
    }

    @Test
    void shouldReturnFalseWhenProductNotAvailableForPurchase() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.makeUnavailableForPurchase();
        
        assertFalse(product.isStockAvailable(50));
    }

    @Test
    void shouldReturnFalseWhenProductDiscontinued() {
        Product product = Product.create(validSku, "Test", "Description", validPrice, validStock);
        product.discontinue();
        
        assertFalse(product.isStockAvailable(50));
    }

    // ==== Reconstitution Tests ====

    @Test
    void shouldReconstituteProduct() {
        ProductId id = ProductId.generate();
        Product product = Product.reconstitute(
            id, validSku, "Test", "Description", new java.util.ArrayList<>(), validPrice, validStock,
            true, true, true, false, 5,
            java.time.LocalDateTime.now().minusDays(1),
            java.time.LocalDateTime.now()
        );
        
        assertEquals(id, product.getId());
        assertEquals(validSku, product.getSku());
        assertEquals(5, product.getVersion());
        assertEquals(0, product.getDomainEvents().size());
    }
}
