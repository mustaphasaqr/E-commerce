package com.mustapha.ecommerce.product.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductId Value Object Tests
 * Tests: UUID generation, validation, equality
 */
class ProductIdTest {

    @Test
    void shouldGenerateUniqueProductId() {
        ProductId id1 = ProductId.generate();
        ProductId id2 = ProductId.generate();
        
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void shouldGenerateValidUUID() {
        ProductId id = ProductId.generate();
        
        assertNotNull(id.getValue());
        assertTrue(id.getValue().matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    void shouldCreateFromValidUUID() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        ProductId id = ProductId.of(uuid);
        
        assertNotNull(id);
        assertEquals(uuid, id.getValue());
    }

    @Test
    void shouldRejectNullValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ProductId.of(null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectEmptyString() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ProductId.of("");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectBlankString() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ProductId.of("   ");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectInvalidUUIDFormat() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ProductId.of("not-a-uuid");
        });
        
        assertTrue(exception.getMessage().contains("valid UUID format"));
    }

    @Test
    void shouldRejectInvalidUUIDStructure() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ProductId.of("12345678-1234-1234-1234-123456789012X");
        });
        
        assertTrue(exception.getMessage().contains("valid UUID format"));
    }

    @Test
    void shouldBeEqualWhenValuesMatch() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        ProductId id1 = ProductId.of(uuid);
        ProductId id2 = ProductId.of(uuid);
        
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {
        ProductId id1 = ProductId.of("123e4567-e89b-12d3-a456-426614174000");
        ProductId id2 = ProductId.of("223e4567-e89b-12d3-a456-426614174000");
        
        assertNotEquals(id1, id2);
    }

    @Test
    void shouldBeEqualToItself() {
        ProductId id = ProductId.generate();
        
        assertEquals(id, id);
    }

    @Test
    void shouldNotBeEqualToNull() {
        ProductId id = ProductId.generate();
        
        assertNotEquals(null, id);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        ProductId id = ProductId.of("123e4567-e89b-12d3-a456-426614174000");
        
        assertNotEquals("123e4567-e89b-12d3-a456-426614174000", id);
    }

    @Test
    void shouldReturnValueAsString() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        ProductId id = ProductId.of(uuid);
        
        assertEquals(uuid, id.toString());
    }
}
