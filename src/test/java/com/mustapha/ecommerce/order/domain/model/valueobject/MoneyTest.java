package com.mustapha.ecommerce.order.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Money Value Object Tests
 * 
 * Focus: Business rules, immutability, precision, domain operations
 * Reviewer: "Value Objects must be immutable and validate on construction"
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Nested
    @DisplayName("Construction and Validation")
    class ConstructionTests {
        
        @Test
        @DisplayName("Should create Money with valid amount")
        void shouldCreateMoneyWithValidAmount() {
            // When
            Money money = new Money(100.0);
            
            // Then
            assertNotNull(money);
            assertEquals(new BigDecimal("100.00"), money.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should create Money from BigDecimal")
        void shouldCreateMoneyFromBigDecimal() {
            // When
            Money money = new Money(new BigDecimal("99.99"));
            
            // Then
            assertEquals(new BigDecimal("99.99"), money.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should fail when amount is null")
        void shouldFailWhenAmountIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Money((BigDecimal) null);
            }, "Amount cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when amount is negative")
        void shouldFailWhenAmountIsNegative() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Money(-10.0);
            }, "Amount cannot be negative");
        }
        
        @Test
        @DisplayName("Should accept zero amount")
        void shouldAcceptZeroAmount() {
            // When
            Money money = new Money(0.0);
            
            // Then
            assertEquals(new BigDecimal("0.00"), money.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should round to 2 decimal places")
        void shouldRoundToTwoDecimalPlaces() {
            // When
            Money money = new Money(10.999);
            
            // Then - should round to 11.00
            assertEquals(new BigDecimal("11.00"), money.getAmountAsBigDecimal());
        }
    }

    @Nested
    @DisplayName("Addition Operations")
    class AdditionTests {
        
        @Test
        @DisplayName("Should add two Money amounts")
        void shouldAddTwoMoneyAmounts() {
            // Given
            Money money1 = new Money(50.0);
            Money money2 = new Money(30.0);
            
            // When
            Money result = money1.add(money2);
            
            // Then
            assertEquals(new BigDecimal("80.00"), result.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should be immutable - original unchanged after add")
        void shouldBeImmutableAfterAdd() {
            // Given
            Money original = new Money(100.0);
            Money toAdd = new Money(50.0);
            
            // When
            Money result = original.add(toAdd);
            
            // Then - original unchanged
            assertEquals(new BigDecimal("100.00"), original.getAmountAsBigDecimal());
            assertEquals(new BigDecimal("150.00"), result.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should fail when adding null Money")
        void shouldFailWhenAddingNull() {
            // Given
            Money money = new Money(100.0);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                money.add(null);
            }, "Cannot add null Money");
        }
        
        @Test
        @DisplayName("Should add zero correctly")
        void shouldAddZeroCorrectly() {
            // Given
            Money money = new Money(100.0);
            Money zero = new Money(0.0);
            
            // When
            Money result = money.add(zero);
            
            // Then
            assertEquals(new BigDecimal("100.00"), result.getAmountAsBigDecimal());
        }
    }

    @Nested
    @DisplayName("Subtraction Operations")
    class SubtractionTests {
        
        @Test
        @DisplayName("Should subtract Money amounts")
        void shouldSubtractMoneyAmounts() {
            // Given
            Money money1 = new Money(100.0);
            Money money2 = new Money(30.0);
            
            // When
            Money result = money1.subtract(money2);
            
            // Then
            assertEquals(new BigDecimal("70.00"), result.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should be immutable - original unchanged after subtract")
        void shouldBeImmutableAfterSubtract() {
            // Given
            Money original = new Money(100.0);
            Money toSubtract = new Money(30.0);
            
            // When
            Money result = original.subtract(toSubtract);
            
            // Then - original unchanged
            assertEquals(new BigDecimal("100.00"), original.getAmountAsBigDecimal());
            assertEquals(new BigDecimal("70.00"), result.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should fail when subtracting more than available")
        void shouldFailWhenSubtractingTooMuch() {
            // Given
            Money money1 = new Money(50.0);
            Money money2 = new Money(100.0);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                money1.subtract(money2);
            }, "Cannot subtract more than available");
        }
        
        @Test
        @DisplayName("Should fail when subtracting null Money")
        void shouldFailWhenSubtractingNull() {
            // Given
            Money money = new Money(100.0);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                money.subtract(null);
            }, "Cannot subtract null Money");
        }
        
        @Test
        @DisplayName("Should allow subtracting exact amount (result = 0)")
        void shouldAllowSubtractingExactAmount() {
            // Given
            Money money = new Money(100.0);
            
            // When
            Money result = money.subtract(new Money(100.0));
            
            // Then
            assertEquals(new BigDecimal("0.00"), result.getAmountAsBigDecimal());
        }
    }

    @Nested
    @DisplayName("Multiplication Operations")
    class MultiplicationTests {
        
        @Test
        @DisplayName("Should multiply by double factor")
        void shouldMultiplyByDoubleFactor() {
            // Given
            Money money = new Money(50.0);
            
            // When
            Money result = money.multiply(2.5);
            
            // Then
            assertEquals(new BigDecimal("125.00"), result.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should multiply by integer quantity")
        void shouldMultiplyByIntegerQuantity() {
            // Given
            Money money = new Money(25.0);
            
            // When
            Money result = money.multiply(3);
            
            // Then
            assertEquals(new BigDecimal("75.00"), result.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should be immutable - original unchanged after multiply")
        void shouldBeImmutableAfterMultiply() {
            // Given
            Money original = new Money(100.0);
            
            // When
            Money result = original.multiply(2);
            
            // Then - original unchanged
            assertEquals(new BigDecimal("100.00"), original.getAmountAsBigDecimal());
            assertEquals(new BigDecimal("200.00"), result.getAmountAsBigDecimal());
        }
        
        @Test
        @DisplayName("Should fail when multiplying by negative factor")
        void shouldFailWhenMultiplyingByNegativeFactor() {
            // Given
            Money money = new Money(100.0);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                money.multiply(-2.0);
            }, "Cannot multiply by negative factor");
        }
        
        @Test
        @DisplayName("Should fail when multiplying by negative quantity")
        void shouldFailWhenMultiplyingByNegativeQuantity() {
            // Given
            Money money = new Money(100.0);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                money.multiply(-5);
            }, "Cannot multiply by negative quantity");
        }
        
        @Test
        @DisplayName("Should multiply by zero")
        void shouldMultiplyByZero() {
            // Given
            Money money = new Money(100.0);
            
            // When
            Money result = money.multiply(0);
            
            // Then
            assertEquals(new BigDecimal("0.00"), result.getAmountAsBigDecimal());
        }
    }

    @Nested
    @DisplayName("Comparison Operations")
    class ComparisonTests {
        
        @Test
        @DisplayName("Should compare greater than")
        void shouldCompareGreaterThan() {
            // Given
            Money money1 = new Money(100.0);
            Money money2 = new Money(50.0);
            
            // When/Then
            assertTrue(money1.isGreaterThan(money2));
            assertFalse(money2.isGreaterThan(money1));
        }
        
        @Test
        @DisplayName("Should compare less than")
        void shouldCompareLessThan() {
            // Given
            Money money1 = new Money(50.0);
            Money money2 = new Money(100.0);
            
            // When/Then
            assertTrue(money1.isLessThan(money2));
            assertFalse(money2.isLessThan(money1));
        }
        
        @Test
        @DisplayName("Should compare greater than or equal")
        void shouldCompareGreaterThanOrEqual() {
            // Given
            Money money1 = new Money(100.0);
            Money money2 = new Money(100.0);
            Money money3 = new Money(50.0);
            
            // When/Then
            assertTrue(money1.isGreaterThanOrEqual(money2)); // equal
            assertTrue(money1.isGreaterThanOrEqual(money3)); // greater
            assertFalse(money3.isGreaterThanOrEqual(money1));
        }
        
        @Test
        @DisplayName("Should compare less than or equal")
        void shouldCompareLessThanOrEqual() {
            // Given
            Money money1 = new Money(50.0);
            Money money2 = new Money(50.0);
            Money money3 = new Money(100.0);
            
            // When/Then
            assertTrue(money1.isLessThanOrEqual(money2)); // equal
            assertTrue(money1.isLessThanOrEqual(money3)); // less
            assertFalse(money3.isLessThanOrEqual(money1));
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal when amounts are same")
        void shouldBeEqualWhenAmountsAreSame() {
            // Given
            Money money1 = new Money(100.0);
            Money money2 = new Money(100.0);
            
            // When/Then
            assertEquals(money1, money2);
            assertEquals(money1.hashCode(), money2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when amounts differ")
        void shouldNotBeEqualWhenAmountsDiffer() {
            // Given
            Money money1 = new Money(100.0);
            Money money2 = new Money(50.0);
            
            // When/Then
            assertNotEquals(money1, money2);
        }
        
        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            Money money = new Money(100.0);
            
            // When/Then
            assertEquals(money, money);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Money money = new Money(100.0);
            
            // When/Then
            assertNotEquals(money, null);
        }
        
        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Money money = new Money(100.0);
            String notMoney = "100.0";
            
            // When/Then
            assertNotEquals(money, notMoney);
        }
        
        @Test
        @DisplayName("Should handle scale differences correctly")
        void shouldHandleScaleDifferencesCorrectly() {
            // Given
            Money money1 = new Money(100.0);
            Money money2 = new Money(100.00);
            
            // When/Then
            assertEquals(money1, money2);
        }
    }

    @Nested
    @DisplayName("toString() and Formatting")
    class FormattingTests {
        
        @Test
        @DisplayName("Should format toString correctly")
        void shouldFormatToStringCorrectly() {
            // Given
            Money money = new Money(100.50);
            
            // When
            String result = money.toString();
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("100.50"));
        }
        
        @Test
        @DisplayName("Should handle zero in toString")
        void shouldHandleZeroInToString() {
            // Given
            Money money = new Money(0.0);
            
            // When
            String result = money.toString();
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("0.00"));
        }
    }
}

