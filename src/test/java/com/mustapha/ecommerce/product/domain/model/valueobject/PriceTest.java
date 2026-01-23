package com.mustapha.ecommerce.product.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Price Value Object Tests
 * Tests: Validation, currency handling, comparisons, arithmetic
 */
class PriceTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateValidPrice() {
        Price price = Price.of(new BigDecimal("99.99"), USD);
        
        assertNotNull(price);
        assertEquals(new BigDecimal("99.99"), price.getAmount());
        assertEquals(USD, price.getCurrency());
    }

    @Test
    void shouldCreateFromString() {
        Price price = Price.of(new BigDecimal("19.99"), "USD");
        
        assertEquals(new BigDecimal("19.99"), price.getAmount());
        assertEquals("USD", price.getCurrencyCode());
    }

    @Test
    void shouldEnforceMinimumPrice() {
        Price price = Price.of(new BigDecimal("0.01"), USD);
        
        assertEquals(new BigDecimal("0.01"), price.getAmount());
    }

    @Test
    void shouldRoundToTwoDecimals() {
        Price price = Price.of(new BigDecimal("19.999"), USD);
        
        assertEquals(new BigDecimal("20.00"), price.getAmount());
    }

    @Test
    void shouldRoundHalfUp() {
        Price price = Price.of(new BigDecimal("19.995"), USD);
        
        assertEquals(new BigDecimal("20.00"), price.getAmount());
    }

    @Test
    void shouldRejectNegativePrice() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Price.of(new BigDecimal("-10.00"), USD);
        });
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void shouldRejectZeroPrice() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Price.of(BigDecimal.ZERO, USD);
        });
        
        assertTrue(exception.getMessage().contains("at least 0.01"));
    }

    @Test
    void shouldRejectBelowMinimum() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Price.of(new BigDecimal("0.009"), USD);
        });
        
        assertTrue(exception.getMessage().contains("at least 0.01"));
    }

    @Test
    void shouldRejectNullCurrency() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Price.of(new BigDecimal("10.00"), (Currency) null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldEnsureSameCurrencyPasses() {
        Price price1 = Price.of(new BigDecimal("10.00"), USD);
        Price price2 = Price.of(new BigDecimal("20.00"), USD);
        
        assertDoesNotThrow(() -> price1.ensureSameCurrency(price2));
    }

    @Test
    void shouldEnsureSameCurrencyFails() {
        Price price1 = Price.of(new BigDecimal("10.00"), USD);
        Price price2 = Price.of(new BigDecimal("20.00"), EUR);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            price1.ensureSameCurrency(price2);
        });
        
        assertTrue(exception.getMessage().contains("different currencies"));
        assertTrue(exception.getMessage().contains("USD"));
        assertTrue(exception.getMessage().contains("EUR"));
    }

    @Test
    void shouldReturnAmountAsDouble() {
        Price price = Price.of(new BigDecimal("99.99"), USD);
        
        assertEquals(99.99, price.getAmountAsDouble(), 0.001);
    }

    @Test
    void shouldGetCurrencyCode() {
        Price price = Price.of(new BigDecimal("10.00"), USD);
        
        assertEquals("USD", price.getCurrencyCode());
    }

    @Test
    void shouldBeEqualWhenSameAmountAndCurrency() {
        Price price1 = Price.of(new BigDecimal("19.99"), USD);
        Price price2 = Price.of(new BigDecimal("19.99"), USD);
        
        assertEquals(price1, price2);
        assertEquals(price1.hashCode(), price2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenAmountDiffers() {
        Price price1 = Price.of(new BigDecimal("19.99"), USD);
        Price price2 = Price.of(new BigDecimal("29.99"), USD);
        
        assertNotEquals(price1, price2);
    }

    @Test
    void shouldNotBeEqualWhenCurrencyDiffers() {
        Price price1 = Price.of(new BigDecimal("19.99"), USD);
        Price price2 = Price.of(new BigDecimal("19.99"), EUR);
        
        assertNotEquals(price1, price2);
    }

    @Test
    void shouldBeEqualToItself() {
        Price price = Price.of(new BigDecimal("19.99"), USD);
        
        assertEquals(price, price);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Price price = Price.of(new BigDecimal("19.99"), USD);
        
        assertNotEquals(null, price);
    }

    @Test
    void shouldFormatToString() {
        Price price = Price.of(new BigDecimal("99.99"), USD);
        
        String formatted = price.toString();
        assertTrue(formatted.contains("99.99"));
        assertTrue(formatted.contains("USD"));
    }

    @Test
    void shouldSupportMultipleCurrencies() {
        Price usd = Price.of(new BigDecimal("10.00"), Currency.getInstance("USD"));
        Price eur = Price.of(new BigDecimal("10.00"), Currency.getInstance("EUR"));
        Price gbp = Price.of(new BigDecimal("10.00"), Currency.getInstance("GBP"));
        
        assertEquals("USD", usd.getCurrencyCode());
        assertEquals("EUR", eur.getCurrencyCode());
        assertEquals("GBP", gbp.getCurrencyCode());
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldCreateFromDeprecatedDoubleMethod() {
        Price price = Price.of(19.99, "USD");
        
        assertEquals(new BigDecimal("19.99"), price.getAmount());
    }
}
