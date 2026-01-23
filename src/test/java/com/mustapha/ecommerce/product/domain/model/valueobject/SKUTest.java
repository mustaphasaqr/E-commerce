package com.mustapha.ecommerce.product.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SKU Value Object Tests
 * Tests: Validation, normalization, equality, business rules
 */
class SKUTest {

    @Test
    void shouldCreateValidSKU() {
        SKU sku = SKU.of("PROD-123");
        
        assertNotNull(sku);
        assertEquals("PROD-123", sku.getValue());
    }

    @Test
    void shouldNormalizeSKUToUppercase() {
        SKU sku = SKU.of("prod-123");
        
        assertEquals("PROD-123", sku.getValue());
    }

    @Test
    void shouldTrimWhitespace() {
        SKU sku = SKU.of("  PROD-123  ");
        
        assertEquals("PROD-123", sku.getValue());
    }

    @Test
    void shouldAcceptMinimumLength() {
        SKU sku = SKU.of("ABC123");
        
        assertEquals("ABC123", sku.getValue());
    }

    @Test
    void shouldAcceptMaximumLength() {
        SKU sku = SKU.of("ABCDEF-123456789-ABCDEF-1234");
        
        assertNotNull(sku);
    }

    @Test
    void shouldAcceptLettersNumbersAndHyphens() {
        SKU sku = SKU.of("PROD-ABC-123-XYZ");
        
        assertEquals("PROD-ABC-123-XYZ", sku.getValue());
    }

    @Test
    void shouldRejectNullValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SKU.of(null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectEmptyString() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SKU.of("");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectBlankString() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SKU.of("   ");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void shouldRejectTooShort() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SKU.of("ABC12");
        });
        
        assertTrue(exception.getMessage().contains("6-30 characters"));
    }

    @Test
    void shouldRejectTooLong() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SKU.of("ABCDEF-123456789-ABCDEF-1234567");  // 33 chars - clearly exceeds 30 limit
        });
        
        assertTrue(exception.getMessage().contains("6-30 characters"));
    }

    @Test
    void shouldAcceptAndConvertLowercaseToUppercase() {
        // SKU implementation auto-converts to uppercase for user convenience
        SKU sku = SKU.of("prod-abc");
        
        assertEquals("PROD-ABC", sku.getValue());
    }

    @Test
    void shouldRejectSpecialCharacters() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SKU.of("PROD@123");
        });
        
        assertTrue(exception.getMessage().contains("uppercase letters"));
    }

    @Test
    void shouldRejectSpaces() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SKU.of("PROD 123");
        });
        
        assertTrue(exception.getMessage().contains("uppercase letters"));
    }

    @Test
    void shouldBeEqualWhenValuesMatch() {
        SKU sku1 = SKU.of("PROD-123");
        SKU sku2 = SKU.of("PROD-123");
        
        assertEquals(sku1, sku2);
        assertEquals(sku1.hashCode(), sku2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {
        SKU sku1 = SKU.of("PROD-123");
        SKU sku2 = SKU.of("PROD-456");
        
        assertNotEquals(sku1, sku2);
    }

    @Test
    void shouldBeEqualToItself() {
        SKU sku = SKU.of("PROD-123");
        
        assertEquals(sku, sku);
    }

    @Test
    void shouldNotBeEqualToNull() {
        SKU sku = SKU.of("PROD-123");
        
        assertNotEquals(null, sku);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        SKU sku = SKU.of("PROD-123");
        
        assertNotEquals("PROD-123", sku);
    }

    @Test
    void shouldReturnValueAsString() {
        SKU sku = SKU.of("PROD-123");
        
        assertEquals("PROD-123", sku.toString());
    }

    @Test
    void shouldNormalizeBeforeValidation() {
        SKU sku = SKU.of("  prod-123  ");
        
        assertEquals("PROD-123", sku.getValue());
    }
}
