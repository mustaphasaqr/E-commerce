package com.mustapha.ecommerce.user.domain.service;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Common Password Checker
 * 
 * Prevents users from choosing commonly used passwords that are easily guessable.
 * Uses a curated list of the most common passwords from data breaches.
 * 
 * Sources:
 * - SplashData "Worst Passwords" annual list
 * - Have I Been Pwned common passwords
 * - NCSC (UK National Cyber Security Centre) research
 * 
 * Why This Matters:
 * - 60% of breaches involve weak/default passwords
 * - Attackers try common passwords first (dictionary attacks)
 * - Even with BCrypt, "password123" is crackable if hash is stolen
 * 
 * Alternative Approaches (for larger lists):
 * - Load from file (resources/common-passwords.txt)
 * - Use Bloom filter for memory efficiency
 * - Check against password dictionaries (10,000+ passwords)
 */
@Service
public class CommonPasswordChecker {
    
    /**
     * Top 100 most common passwords.
     * Expanded from typical "top 10" to catch more variations.
     * 
     * NOTE: In production, load this from external file or database
     * for easier updates without code changes.
     */
    private static final Set<String> COMMON_PASSWORDS = Set.of(
        // Top 10 (always the same every year)
        "password", "123456", "12345678", "qwerty", "abc123",
        "monkey", "1234567", "letmein", "trustno1", "dragon",
        
        // Variations of "password"
        "password1", "password123", "Password", "Password1", "password!", 
        "passw0rd", "pa$$word", "p@ssword", "p@$$w0rd",
        
        // Sequential numbers
        "123456789", "12345", "1234", "111111", "000000",
        "123123", "654321", "987654321", "1234567890",
        
        // Sequential letters (keyboard patterns)
        "qwertyui", "asdfgh", "zxcvbn", "qwerty123", "qwerty1",
        "1q2w3e4r", "1qaz2wsx", "qwertyuiop", "asdfghjkl",
        
        // Common words + numbers
        "welcome", "welcome1", "admin", "admin123", "root",
        "user", "test", "test123", "guest", "demo",
        
        // Names/Places
        "london", "shadow", "master", "michael", "jennifer",
        "jordan", "harley", "charlie", "football", "baseball",
        
        // Years/Dates
        "2024", "2023", "2022", "2021", "2020",
        "1990", "1991", "1992", "1993", "1994",
        
        // Brands/Products
        "samsung", "chelsea", "arsenal", "liverpool", "google",
        "facebook", "twitter", "instagram", "youtube",
        
        // Simple phrases
        "iloveyou", "sunshine", "princess", "starwars", "pokemon",
        "superman", "batman", "hello", "freedom", "whatever",
        
        // Weak patterns
        "password1234", "qwerty12345", "abc12345", "pass1234",
        "1234qwer", "welcome123", "admin1234", "123abc",
        
        // Default credentials (IoT devices, routers)
        "admin1", "admin123!", "changeme", "default",
        "12341234", "passpass", "adminadmin",
        
        // Single words (too simple)
        "flower", "cookie", "soccer", "hockey", "ranger",
        "summer", "winter", "spring", "autumn"
    );
    
    /**
     * Checks if password is in the common passwords list.
     * 
     * Security: Case-insensitive check to catch "Password", "PASSWORD", etc.
     * 
     * @param password Plain text password
     * @return true if password is common (should be rejected)
     */
    public boolean isCommonPassword(String password) {
        if (password == null || password.isBlank()) {
            return false; // Let other validators handle null/blank
        }
        
        // Case-insensitive check
        String lowerPassword = password.toLowerCase();
        
        // Direct match
        if (COMMON_PASSWORDS.contains(lowerPassword)) {
            return true;
        }
        
        // Check if password contains common password as substring
        // E.g., "MyPassword123" contains "password"
        for (String commonPassword : COMMON_PASSWORDS) {
            if (lowerPassword.contains(commonPassword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validates password is not common.
     * Throws exception with helpful message if common.
     * 
     * @param password Plain text password
     * @throws IllegalArgumentException if password is too common
     */
    public void validateNotCommon(String password) {
        if (isCommonPassword(password)) {
            throw new IllegalArgumentException(
                "Password is too common and easily guessable. " +
                "Please choose a more unique password that doesn't include common words or patterns."
            );
        }
    }
    
    /**
     * Gets the size of the common passwords list.
     * Useful for testing and monitoring.
     */
    public int getCommonPasswordsCount() {
        return COMMON_PASSWORDS.size();
    }
}
