package com.mustapha.ecommerce.user.domain.model.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @Test
    void of_ValidEmail_CreatesEmail() {
        Email email = Email.of("test@example.com");

        assertThat(email).isNotNull();
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    void of_NormalizesToLowercase() {
        Email email = Email.of("Test@Example.COM");

        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    void of_TrimsWhitespace() {
        Email email = Email.of("  test@example.com  ");

        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    void of_NullEmail_ThrowsException() {
        assertThatThrownBy(() -> Email.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void of_BlankEmail_ThrowsException() {
        assertThatThrownBy(() -> Email.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void of_InvalidFormat_ThrowsException() {
        assertThatThrownBy(() -> Email.of("invalid-email"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    void of_MissingAtSign_ThrowsException() {
        assertThatThrownBy(() -> Email.of("testexample.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    void of_MissingDomain_ThrowsException() {
        assertThatThrownBy(() -> Email.of("test@"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    void of_TooLong_ThrowsException() {
        String longEmail = "a".repeat(250) + "@example.com";
        
        assertThatThrownBy(() -> Email.of(longEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot exceed 255 characters");
    }

    @Test
    void equals_SameValue_ReturnsTrue() {
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("test@example.com");

        assertThat(email1).isEqualTo(email2);
    }

    @Test
    void equals_DifferentCase_ReturnsTrueAfterNormalization() {
        Email email1 = Email.of("Test@Example.com");
        Email email2 = Email.of("test@example.COM");

        assertThat(email1).isEqualTo(email2);
    }

    @Test
    void equals_DifferentValue_ReturnsFalse() {
        Email email1 = Email.of("test1@example.com");
        Email email2 = Email.of("test2@example.com");

        assertThat(email1).isNotEqualTo(email2);
    }

    @Test
    void hashCode_SameValue_ReturnsSameHash() {
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("test@example.com");

        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    void toString_ReturnsValue() {
        Email email = Email.of("test@example.com");

        assertThat(email.toString()).contains("test@example.com");
    }
}
