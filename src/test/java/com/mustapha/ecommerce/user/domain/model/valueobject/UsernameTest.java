package com.mustapha.ecommerce.user.domain.model.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UsernameTest {

    @Test
    void of_ValidUsername_CreatesUsername() {
        Username username = Username.of("testuser");

        assertThat(username).isNotNull();
        assertThat(username.getValue()).isEqualTo("testuser");
    }

    @Test
    void of_NormalizesToLowercase() {
        Username username = Username.of("TestUser");

        assertThat(username.getValue()).isEqualTo("testuser");
    }

    @Test
    void of_WithUnderscore_CreatesUsername() {
        Username username = Username.of("test_user");

        assertThat(username.getValue()).isEqualTo("test_user");
    }

    @Test
    void of_WithHyphen_CreatesUsername() {
        Username username = Username.of("test-user");

        assertThat(username.getValue()).isEqualTo("test-user");
    }

    @Test
    void of_MinLength_CreatesUsername() {
        Username username = Username.of("abc");

        assertThat(username.getValue()).isEqualTo("abc");
    }

    @Test
    void of_MaxLength_CreatesUsername() {
        String name = "a".repeat(30);
        Username username = Username.of(name);

        assertThat(username.getValue()).isEqualTo(name);
    }

    @Test
    void of_NullUsername_ThrowsException() {
        assertThatThrownBy(() -> Username.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void of_BlankUsername_ThrowsException() {
        assertThatThrownBy(() -> Username.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void of_TooShort_ThrowsException() {
        assertThatThrownBy(() -> Username.of("ab"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 3 characters");
    }

    @Test
    void of_TooLong_ThrowsException() {
        String name = "a".repeat(31);
        
        assertThatThrownBy(() -> Username.of(name))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot exceed 30 characters");
    }

    @Test
    void of_StartsWithSpecialChar_ThrowsException() {
        assertThatThrownBy(() -> Username.of("_testuser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("start and end with a letter or number");
    }

    @Test
    void of_EndsWithSpecialChar_ThrowsException() {
        assertThatThrownBy(() -> Username.of("testuser_"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("start and end with a letter or number");
    }

    @Test
    void of_ConsecutiveSpecialChars_ThrowsException() {
        assertThatThrownBy(() -> Username.of("test__user"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("start and end with a letter or number");
    }

    @Test
    void of_InvalidCharacters_ThrowsException() {
        assertThatThrownBy(() -> Username.of("test@user"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("start and end with a letter or number");
    }

    @Test
    void of_ReservedUsername_ThrowsException() {
        assertThatThrownBy(() -> Username.of("admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reserved");
    }

    @Test
    void of_ReservedUsernameUppercase_ThrowsException() {
        assertThatThrownBy(() -> Username.of("ADMIN"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reserved");
    }

    @Test
    void equals_SameValue_ReturnsTrue() {
        Username username1 = Username.of("testuser");
        Username username2 = Username.of("testuser");

        assertThat(username1).isEqualTo(username2);
    }

    @Test
    void equals_DifferentCase_ReturnsTrueAfterNormalization() {
        Username username1 = Username.of("TestUser");
        Username username2 = Username.of("testuser");

        assertThat(username1).isEqualTo(username2);
    }

    @Test
    void equals_DifferentValue_ReturnsFalse() {
        Username username1 = Username.of("testuser1");
        Username username2 = Username.of("testuser2");

        assertThat(username1).isNotEqualTo(username2);
    }

    @Test
    void hashCode_SameValue_ReturnsSameHash() {
        Username username1 = Username.of("testuser");
        Username username2 = Username.of("testuser");

        assertThat(username1.hashCode()).isEqualTo(username2.hashCode());
    }
}
