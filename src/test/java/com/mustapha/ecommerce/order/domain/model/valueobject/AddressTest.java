package com.mustapha.ecommerce.order.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Address Value Object Tests
 * 
 * Focus: Validation, immutability, business rules
 * Reviewer: "خليه بسيط من غير Validation مجنونة دلوقتي"
 */
@DisplayName("Address Value Object Tests")
class AddressTest {

    @Nested
    @DisplayName("Construction and Validation")
    class ConstructionTests {
        
        @Test
        @DisplayName("Should create Address with valid data")
        void shouldCreateAddressWithValidData() {
            // When
            Address address = new Address("123 Main St", "Cairo", "Egypt");
            
            // Then
            assertNotNull(address);
            assertEquals("123 Main St", address.getStreet());
            assertEquals("Cairo", address.getCity());
            assertEquals("Egypt", address.getCountry());
        }
        
        @Test
        @DisplayName("Should fail when street is null")
        void shouldFailWhenStreetIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address(null, "Cairo", "Egypt");
            }, "Street cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when street is blank")
        void shouldFailWhenStreetIsBlank() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address("   ", "Cairo", "Egypt");
            }, "Street cannot be blank");
        }
        
        @Test
        @DisplayName("Should fail when street exceeds max length")
        void shouldFailWhenStreetExceedsMaxLength() {
            // Given
            String tooLong = "x".repeat(201);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address(tooLong, "Cairo", "Egypt");
            }, "Street cannot exceed 200 characters");
        }
        
        @Test
        @DisplayName("Should fail when city is null")
        void shouldFailWhenCityIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address("123 Main St", null, "Egypt");
            }, "City cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when city is blank")
        void shouldFailWhenCityIsBlank() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address("123 Main St", "   ", "Egypt");
            }, "City cannot be blank");
        }
        
        @Test
        @DisplayName("Should fail when city exceeds max length")
        void shouldFailWhenCityExceedsMaxLength() {
            // Given
            String tooLong = "x".repeat(101);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address("123 Main St", tooLong, "Egypt");
            }, "City cannot exceed 100 characters");
        }
        
        @Test
        @DisplayName("Should fail when country is null")
        void shouldFailWhenCountryIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address("123 Main St", "Cairo", null);
            }, "Country cannot be null");
        }
        
        @Test
        @DisplayName("Should fail when country is blank")
        void shouldFailWhenCountryIsBlank() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address("123 Main St", "Cairo", "   ");
            }, "Country cannot be blank");
        }
        
        @Test
        @DisplayName("Should fail when country exceeds max length")
        void shouldFailWhenCountryExceedsMaxLength() {
            // Given
            String tooLong = "x".repeat(101);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> {
                new Address("123 Main St", "Cairo", tooLong);
            }, "Country cannot exceed 100 characters");
        }
        
        @Test
        @DisplayName("Should accept values at max length")
        void shouldAcceptValuesAtMaxLength() {
            // Given
            String maxStreet = "x".repeat(200);
            String maxCity = "x".repeat(100);
            String maxCountry = "x".repeat(100);
            
            // When
            Address address = new Address(maxStreet, maxCity, maxCountry);
            
            // Then
            assertNotNull(address);
            assertEquals(200, address.getStreet().length());
            assertEquals(100, address.getCity().length());
            assertEquals(100, address.getCountry().length());
        }
    }

    @Nested
    @DisplayName("Business Methods")
    class BusinessMethodTests {
        
        @Test
        @DisplayName("Should format full address correctly")
        void shouldFormatFullAddressCorrectly() {
            // Given
            Address address = new Address("123 Main St", "Cairo", "Egypt");
            
            // When
            String fullAddress = address.getFullAddress();
            
            // Then
            assertEquals("123 Main St, Cairo, Egypt", fullAddress);
        }
        
        @Test
        @DisplayName("Should handle special characters in full address")
        void shouldHandleSpecialCharactersInFullAddress() {
            // Given
            Address address = new Address("123 Al-Azhar St.", "Al-Qāhirah", "مصر");
            
            // When
            String fullAddress = address.getFullAddress();
            
            // Then
            assertTrue(fullAddress.contains("123 Al-Azhar St."));
            assertTrue(fullAddress.contains("Al-Qāhirah"));
            assertTrue(fullAddress.contains("مصر"));
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Given
            Address address1 = new Address("123 Main St", "Cairo", "Egypt");
            Address address2 = new Address("123 Main St", "Cairo", "Egypt");
            
            // When/Then
            assertEquals(address1, address2);
            assertEquals(address1.hashCode(), address2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal when street differs")
        void shouldNotBeEqualWhenStreetDiffers() {
            // Given
            Address address1 = new Address("123 Main St", "Cairo", "Egypt");
            Address address2 = new Address("456 Other St", "Cairo", "Egypt");
            
            // When/Then
            assertNotEquals(address1, address2);
        }
        
        @Test
        @DisplayName("Should not be equal when city differs")
        void shouldNotBeEqualWhenCityDiffers() {
            // Given
            Address address1 = new Address("123 Main St", "Cairo", "Egypt");
            Address address2 = new Address("123 Main St", "Alexandria", "Egypt");
            
            // When/Then
            assertNotEquals(address1, address2);
        }
        
        @Test
        @DisplayName("Should not be equal when country differs")
        void shouldNotBeEqualWhenCountryDiffers() {
            // Given
            Address address1 = new Address("123 Main St", "Cairo", "Egypt");
            Address address2 = new Address("123 Main St", "Cairo", "Jordan");
            
            // When/Then
            assertNotEquals(address1, address2);
        }
        
        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            Address address = new Address("123 Main St", "Cairo", "Egypt");
            
            // When/Then
            assertEquals(address, address);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Address address = new Address("123 Main St", "Cairo", "Egypt");
            
            // When/Then
            assertNotEquals(address, null);
        }
        
        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Address address = new Address("123 Main St", "Cairo", "Egypt");
            String notAddress = "123 Main St, Cairo, Egypt";
            
            // When/Then
            assertNotEquals(address, notAddress);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable - fields cannot change")
        void shouldBeImmutable() {
            // Given
            Address address = new Address("123 Main St", "Cairo", "Egypt");
            
            // When - get values multiple times
            String street1 = address.getStreet();
            String street2 = address.getStreet();
            String city1 = address.getCity();
            String city2 = address.getCity();
            String country1 = address.getCountry();
            String country2 = address.getCountry();
            
            // Then - values should be same
            assertEquals(street1, street2);
            assertEquals(city1, city2);
            assertEquals(country1, country2);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {
        
        @Test
        @DisplayName("Should contain all fields in toString")
        void shouldContainAllFieldsInToString() {
            // Given
            Address address = new Address("123 Main St", "Cairo", "Egypt");
            
            // When
            String result = address.toString();
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("123 Main St"));
            assertTrue(result.contains("Cairo"));
            assertTrue(result.contains("Egypt"));
        }
    }
}
