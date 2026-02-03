package com.mustapha.ecommerce.user.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Password Value Object Tests")
class PasswordTest {

    @Test
    @DisplayName("Should create Password from hashed value")
    void shouldCreatePasswordFromHashedValue() {
        String hashedValue = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        Password password = Password.fromHashed(hashedValue);

        assertThat(password).isNotNull();
        assertThat(password.getHashedValue()).isEqualTo(hashedValue);
    }

    @Test
    @DisplayName("Should throw exception when hashed value is null")
    void shouldThrowExceptionWhenHashedValueIsNull() {
        assertThatThrownBy(() -> Password.fromHashed(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("Should throw exception when hashed value is empty")
    void shouldThrowExceptionWhenHashedValueIsEmpty() {
        assertThatThrownBy(() -> Password.fromHashed(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("Should throw exception when hashed value is blank")
    void shouldThrowExceptionWhenHashedValueIsBlank() {
        assertThatThrownBy(() -> Password.fromHashed("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("Should be equal when hashed values are the same")
    void shouldBeEqualWhenHashedValuesAreSame() {
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        Password password1 = Password.fromHashed(hash);
        Password password2 = Password.fromHashed(hash);

        assertThat(password1).isEqualTo(password2);
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when hashed values are different")
    void shouldNotBeEqualWhenHashedValuesAreDifferent() {
        Password password1 = Password.fromHashed("$2a$10$hash1234567890abcdefghijklmnopqrstuvwxyz12");
        Password password2 = Password.fromHashed("$2a$10$hash9876543210zyxwvutsrqponmlkjihgfedcba98");

        assertThat(password1).isNotEqualTo(password2);
    }

    @Test
    @DisplayName("Should not expose password in toString")
    void shouldNotExposePasswordInToString() {
        Password password = Password.fromHashed("$2a$10$secrethashvalue1234567890");

        String toString = password.toString();
        assertThat(toString).doesNotContain("secrethash");
        assertThat(toString).doesNotContain("$2a$10$");
        assertThat(toString).contains("***");
    }

    @Test
    @DisplayName("Should accept valid BCrypt hashed passwords")
    void shouldAcceptValidBCryptHashedPasswords() {
        assertThatNoException().isThrownBy(() -> 
            Password.fromHashed("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"));
        assertThatNoException().isThrownBy(() -> 
            Password.fromHashed("$2b$12$abcdefghijklmnopqrstuvwxyz1234567890ABCD"));
    }

    @Test
    @DisplayName("Should validate plain text password strength")
    void shouldValidatePlainTextPasswordStrength() {
        assertThatNoException().isThrownBy(() -> Password.validate("SecurePass123!"));
        assertThatNoException().isThrownBy(() -> Password.validate("MyP@ssw0rd"));
        assertThatNoException().isThrownBy(() -> Password.validate("Complex1ty!"));
    }

    @Test
    @DisplayName("Should reject weak passwords - too short")
    void shouldRejectWeakPasswordsTooShort() {
        assertThatThrownBy(() -> Password.validate("Short1!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 8 characters");
    }

    @Test
    @DisplayName("Should reject weak passwords - missing uppercase")
    void shouldRejectWeakPasswordsMissingUppercase() {
        assertThatThrownBy(() -> Password.validate("lowercase123!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("uppercase letter");
    }

    @Test
    @DisplayName("Should reject weak passwords - missing lowercase")
    void shouldRejectWeakPasswordsMissingLowercase() {
        assertThatThrownBy(() -> Password.validate("UPPERCASE123!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("lowercase letter");
    }

    @Test
    @DisplayName("Should reject weak passwords - missing digit")
    void shouldRejectWeakPasswordsMissingDigit() {
        assertThatThrownBy(() -> Password.validate("NoDigitsHere!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("digit");
    }

    @Test
    @DisplayName("Should reject weak passwords - missing special character")
    void shouldRejectWeakPasswordsMissingSpecialChar() {
        assertThatThrownBy(() -> Password.validate("NoSpecial123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("special character");
    }
}
