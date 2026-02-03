package com.mustapha.ecommerce.user.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserId Value Object Tests")
class UserIdTest {

    @Test
    @DisplayName("Should create UserId with valid UUID string")
    void shouldCreateUserIdWithValidValue() {
        String value = "550e8400-e29b-41d4-a716-446655440000";

        UserId userId = UserId.of(value);

        assertThat(userId).isNotNull();
        assertThat(userId.toString()).isEqualTo(value);
    }

    @Test
    @DisplayName("Should throw exception when value is null")
    void shouldThrowExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> UserId.of((String) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("Should throw exception when value is empty")
    void shouldThrowExceptionWhenValueIsEmpty() {
        assertThatThrownBy(() -> UserId.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("Should throw exception when value is blank")
    void shouldThrowExceptionWhenValueIsBlank() {
        assertThatThrownBy(() -> UserId.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("Should be equal when values are the same")
    void shouldBeEqualWhenValuesAreSame() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        UserId userId1 = UserId.of(uuid);
        UserId userId2 = UserId.of(uuid);

        assertThat(userId1).isEqualTo(userId2);
        assertThat(userId1.hashCode()).isEqualTo(userId2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when values are different")
    void shouldNotBeEqualWhenValuesAreDifferent() {
        UserId userId1 = UserId.of("550e8400-e29b-41d4-a716-446655440000");
        UserId userId2 = UserId.of("650e8400-e29b-41d4-a716-446655440001");

        assertThat(userId1).isNotEqualTo(userId2);
    }

    @Test
    @DisplayName("Should have readable toString")
    void shouldHaveReadableToString() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        UserId userId = UserId.of(uuid);

        assertThat(userId.toString()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should accept valid UUID formats")
    void shouldAcceptValidUuidFormats() {
        assertThatNoException().isThrownBy(() -> UserId.of("550e8400-e29b-41d4-a716-446655440000"));
        assertThatNoException().isThrownBy(() -> UserId.of("650e8400-e29b-41d4-a716-446655440001"));
        assertThatNoException().isThrownBy(() -> UserId.newId());
    }

    @Test
    @DisplayName("Should throw exception for invalid UUID format")
    void shouldThrowExceptionForInvalidUuidFormat() {
        assertThatThrownBy(() -> UserId.of("invalid-uuid-format"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid user ID format");
    }
}
