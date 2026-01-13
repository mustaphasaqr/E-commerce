package com.mustapha.ecommerce.order.domain.model.valueobject;

import java.util.Objects;

/**
 * Address Value Object
 * 
 * Responsibility: Encapsulate shipping/billing address with validation
 * 
 * Pattern: Value Object
 * - Immutable (final class, final fields)
 * - Validation on construction
 * - Equality by value (not identity)
 * 
 * Business Rules (Simple Version):
 * - Street, city, and country are required
 * - Each field has max length limit
 * - Future: Can add postal code, state, validation patterns
 * 
 * Note: This is a SIMPLE version per reviewer's advice
 * "خليه بسيط من غير Validation مجنونة دلوقتي"
 */
public final class Address {
    
    private final String street;
    private final String city;
    private final String country;
    
    /**
     * Create an Address with basic validation
     * 
     * @param street Street address (required, max 200 chars)
     * @param city City name (required, max 100 chars)
     * @param country Country name (required, max 100 chars)
     * @throws IllegalArgumentException if any field is null, blank, or exceeds max length
     */
    public Address(String street, String city, String country) {
        // Validate street
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank");
        }
        if (street.length() > 200) {
            throw new IllegalArgumentException("Street cannot exceed 200 characters");
        }
        
        // Validate city
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }
        if (city.length() > 100) {
            throw new IllegalArgumentException("City cannot exceed 100 characters");
        }
        
        // Validate country
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or blank");
        }
        if (country.length() > 100) {
            throw new IllegalArgumentException("Country cannot exceed 100 characters");
        }
        
        this.street = street;
        this.city = city;
        this.country = country;
    }
    
    public String getStreet() {
        return street;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getCountry() {
        return country;
    }
    
    /**
     * Get full address as formatted string
     */
    public String getFullAddress() {
        return street + ", " + city + ", " + country;
    }
    
    // Value Object equality - by value, not identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(country, address.country);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(street, city, country);
    }
    
    @Override
    public String toString() {
        return getFullAddress();
    }
}
