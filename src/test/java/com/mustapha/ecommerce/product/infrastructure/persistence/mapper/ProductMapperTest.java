package com.mustapha.ecommerce.product.infrastructure.persistence.mapper;

import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.*;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductMapper Tests
 * Tests: Domain <-> Entity mapping, data integrity, bidirectional conversion
 */
class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    void shouldMapDomainToEntity() {
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Test Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        ProductJpaEntity entity = mapper.toEntity(product);

        assertNotNull(entity);
        assertEquals(product.getId().getValue(), entity.getId());
        assertEquals("PROD-123", entity.getSku());
        assertEquals("Test Product", entity.getName());
        assertEquals("Test Description", entity.getDescription());
        assertEquals(new BigDecimal("99.99"), entity.getPrice());
        assertEquals("USD", entity.getCurrency());
        assertEquals(100, entity.getTotalStock());
        assertEquals(100, entity.getAvailableStock());
        assertEquals(0, entity.getReservedStock());
        assertTrue(entity.isActive());
        assertTrue(entity.isVisible());
        assertTrue(entity.isAvailableForPurchase());
        assertFalse(entity.isDiscontinued());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void shouldMapEntityToDomain() {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId("550e8400-e29b-41d4-a716-446655440000");
        entity.setSku("PROD-456");
        entity.setName("JPA Product");
        entity.setDescription("JPA Description");
        entity.setPrice(new BigDecimal("199.99"));
        entity.setCurrency("EUR");
        entity.setTotalStock(50);
        entity.setAvailableStock(50);
        entity.setReservedStock(0);
        entity.setActive(true);
        entity.setVisible(true);
        entity.setAvailableForPurchase(true);
        entity.setDiscontinued(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setReservations(new HashMap<>());
        entity.setVersion(1L);

        Product product = mapper.toDomain(entity);

        assertNotNull(product);
        assertEquals("550e8400-e29b-41d4-a716-446655440000", product.getId().getValue());
        assertEquals("PROD-456", product.getSku().getValue());
        assertEquals("JPA Product", product.getName());
        assertEquals("JPA Description", product.getDescription());
        assertEquals(new BigDecimal("199.99"), product.getPrice().getAmount());
        assertEquals(Currency.getInstance("EUR"), product.getPrice().getCurrency());
        assertEquals(50, product.getStock().getQuantity());
        assertEquals(50, product.getStock().getAvailableQuantity());
        assertEquals(0, product.getStock().getReservedQuantity());
        assertTrue(product.isActive());
        assertTrue(product.isVisible());
        assertTrue(product.isAvailableForPurchase());
        assertFalse(product.isDiscontinued());
    }

    @Test
    void shouldPreserveReservationsInDomainToEntityMapping() {
        Product product = Product.create(
            SKU.of("PROD-789"),
            "Reserved Product",
            "Description",
            Price.of(new BigDecimal("50.00"), Currency.getInstance("USD")),
            Stock.of(100)
        );
        product.reserveStockForOrder("ORDER-1", 10);
        product.reserveStockForOrder("ORDER-2", 20);
        product.clearDomainEvents();

        ProductJpaEntity entity = mapper.toEntity(product);

        assertNotNull(entity.getReservations());
        assertEquals(2, entity.getReservations().size());
        assertEquals(10, entity.getReservations().get("ORDER-1"));
        assertEquals(20, entity.getReservations().get("ORDER-2"));
        assertEquals(30, entity.getReservedStock());
        assertEquals(70, entity.getAvailableStock());
    }

    @Test
    void shouldPreserveReservationsInEntityToDomainMapping() {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId("550e8400-e29b-41d4-a716-446655440000");
        entity.setSku("PROD-RES");
        entity.setName("Product");
        entity.setDescription("Desc");
        entity.setPrice(new BigDecimal("75.00"));
        entity.setCurrency("USD");
        entity.setTotalStock(200);
        entity.setAvailableStock(150);
        entity.setReservedStock(50);
        entity.setActive(true);
        entity.setVisible(true);
        entity.setAvailableForPurchase(true);
        entity.setDiscontinued(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVersion(1L);

        Map<String, Integer> reservations = new HashMap<>();
        reservations.put("ORDER-100", 25);
        reservations.put("ORDER-200", 25);
        entity.setReservations(reservations);

        Product product = mapper.toDomain(entity);

        assertEquals(50, product.getStock().getReservedQuantity());
        assertEquals(150, product.getStock().getAvailableQuantity());
        assertEquals(2, product.getStock().getReservations().size());
    }

    @Test
    void shouldMapInactiveAndDiscontinuedState() {
        Product product = Product.create(
            SKU.of("PROD-OLD"),
            "Old Product",
            "Legacy",
            Price.of(new BigDecimal("10.00"), Currency.getInstance("USD")),
            Stock.of(5)
        );
        product.deactivate();
        product.discontinue();
        product.clearDomainEvents();

        ProductJpaEntity entity = mapper.toEntity(product);

        assertFalse(entity.isActive());
        assertTrue(entity.isDiscontinued());
    }

    @Test
    void shouldRoundTripConversionPreserveAllData() {
        // Create domain object
        Product original = Product.create(
            SKU.of("ROUND-TRIP"),
            "Round Trip Test",
            "Full cycle conversion",
            Price.of(new BigDecimal("123.45"), Currency.getInstance("GBP")),
            Stock.of(300)
        );
        original.reserveStockForOrder("ORDER-RT", 50);
        original.clearDomainEvents();

        // Domain -> Entity
        ProductJpaEntity entity = mapper.toEntity(original);

        // Entity -> Domain
        Product reconstituted = mapper.toDomain(entity);

        // Verify all data preserved
        assertEquals(original.getId().getValue(), reconstituted.getId().getValue());
        assertEquals(original.getSku().getValue(), reconstituted.getSku().getValue());
        assertEquals(original.getName(), reconstituted.getName());
        assertEquals(original.getDescription(), reconstituted.getDescription());
        assertEquals(original.getPrice().getAmount(), reconstituted.getPrice().getAmount());
        assertEquals(original.getPrice().getCurrency(), reconstituted.getPrice().getCurrency());
        assertEquals(original.getStock().getQuantity(), reconstituted.getStock().getQuantity());
        assertEquals(original.getStock().getAvailableQuantity(), reconstituted.getStock().getAvailableQuantity());
        assertEquals(original.getStock().getReservedQuantity(), reconstituted.getStock().getReservedQuantity());
        assertEquals(original.isActive(), reconstituted.isActive());
        assertEquals(original.isVisible(), reconstituted.isVisible());
        assertEquals(original.isAvailableForPurchase(), reconstituted.isAvailableForPurchase());
        assertEquals(original.isDiscontinued(), reconstituted.isDiscontinued());
    }

    @Test
    void shouldMapEmptyReservations() {
        Product product = Product.create(
            SKU.of("NO-RES"),
            "No Reservations",
            "None",
            Price.of(new BigDecimal("1.00"), Currency.getInstance("USD")),
            Stock.of(10)
        );

        ProductJpaEntity entity = mapper.toEntity(product);

        assertNotNull(entity.getReservations());
        assertTrue(entity.getReservations().isEmpty());
        assertEquals(0, entity.getReservedStock());
    }

    @Test
    void shouldMapMultipleCurrencies() {
        // USD
        Product usdProduct = Product.create(
            SKU.of("USD-PROD"),
            "USD Product",
            "Desc",
            Price.of(new BigDecimal("100.00"), Currency.getInstance("USD")),
            Stock.of(10)
        );
        ProductJpaEntity usdEntity = mapper.toEntity(usdProduct);
        assertEquals("USD", usdEntity.getCurrency());

        // EUR
        Product eurProduct = Product.create(
            SKU.of("EUR-PROD"),
            "EUR Product",
            "Desc",
            Price.of(new BigDecimal("100.00"), Currency.getInstance("EUR")),
            Stock.of(10)
        );
        ProductJpaEntity eurEntity = mapper.toEntity(eurProduct);
        assertEquals("EUR", eurEntity.getCurrency());

        // JPY
        Product jpyProduct = Product.create(
            SKU.of("JPY-PROD"),
            "JPY Product",
            "Desc",
            Price.of(new BigDecimal("10000"), Currency.getInstance("JPY")),
            Stock.of(10)
        );
        ProductJpaEntity jpyEntity = mapper.toEntity(jpyProduct);
        assertEquals("JPY", jpyEntity.getCurrency());
    }

    @Test
    void shouldMapPriceWithPrecision() {
        Product product = Product.create(
            SKU.of("PRECISE"),
            "Precise Price",
            "Desc",
            Price.of(new BigDecimal("99.9999"), Currency.getInstance("USD")),
            Stock.of(1)
        );

        ProductJpaEntity entity = mapper.toEntity(product);

        // Price rounds to 2 decimal places in Price value object
        assertEquals(new BigDecimal("100.00"), entity.getPrice());
    }

    @Test
    void shouldMapTimestamps() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        
        Product product = Product.create(
            SKU.of("TIME-TEST"),
            "Timestamp Test",
            "Desc",
            Price.of(new BigDecimal("1.00"), Currency.getInstance("USD")),
            Stock.of(1)
        );

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        ProductJpaEntity entity = mapper.toEntity(product);

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getCreatedAt().isAfter(beforeCreation));
        assertTrue(entity.getCreatedAt().isBefore(afterCreation));
    }

    @Test
    void shouldMapProductWithNullDescription() {
        Product product = Product.create(
            SKU.of("NULL-DESC"),
            "No Description",
            null,
            Price.of(new BigDecimal("1.00"), Currency.getInstance("USD")),
            Stock.of(1)
        );

        ProductJpaEntity entity = mapper.toEntity(product);

        assertNull(entity.getDescription());
    }

    @Test
    void shouldMapLargeStockQuantities() {
        Product product = Product.create(
            SKU.of("BULK-ITEM"),
            "Bulk Product",
            "Large inventory",
            Price.of(new BigDecimal("0.50"), Currency.getInstance("USD")),
            Stock.of(1000000)
        );

        ProductJpaEntity entity = mapper.toEntity(product);

        assertEquals(1000000, entity.getTotalStock());
        assertEquals(1000000, entity.getAvailableStock());
    }
}
