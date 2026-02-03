package com.mustapha.ecommerce.user.domain.model;

import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.exception.ExpiredTokenException;
import com.mustapha.ecommerce.user.auth.domain.exception.TokenAlreadyUsedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PasswordResetToken Domain Model Tests")
class PasswordResetTokenTest {

    @Test
    @DisplayName("Should create password reset token with valid data")
    void shouldCreatePasswordResetTokenWithValidData() {
        String userId = "USER-123";
        String email = "test@example.com";

        PasswordResetToken token = PasswordResetToken.create(userId, email);

        assertThat(token).isNotNull();
        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getCreatedAt()).isNotNull();
        assertThat(token.getExpiresAt()).isAfter(token.getCreatedAt());
        assertThat(token.isUsed()).isFalse();
        assertThat(token.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("Should reconstitute token from persistence")
    void shouldReconstituteTokenFromPersistence() {
        String tokenValue = "token-123";
        String userId = "USER-123";
        String email = "test@example.com";
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(23);

        PasswordResetToken token = PasswordResetToken.reconstitute(
            tokenValue, userId, email, createdAt, expiresAt, false, null
        );

        assertThat(token.getToken()).isEqualTo(tokenValue);
        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getCreatedAt()).isEqualTo(createdAt);
        assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    @DisplayName("Should mark token as used")
    void shouldMarkTokenAsUsed() {
        PasswordResetToken token = PasswordResetToken.create("USER-123", "test@example.com");

        token.use();

        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when using expired token")
    void shouldThrowExceptionWhenUsingExpiredToken() {
        PasswordResetToken token = PasswordResetToken.reconstitute(
            "token", "USER-123", "test@example.com",
            LocalDateTime.now().minusHours(25),
            LocalDateTime.now().minusHours(1),
            false, null
        );

        assertThatThrownBy(token::use)
            .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    @DisplayName("Should throw exception when using already used token")
    void shouldThrowExceptionWhenUsingAlreadyUsedToken() {
        PasswordResetToken token = PasswordResetToken.create("USER-123", "test@example.com");
        token.use();

        assertThatThrownBy(token::use)
            .isInstanceOf(TokenAlreadyUsedException.class);
    }

    @Test
    @DisplayName("Should have 24-hour validity period")
    void shouldHave24HourValidityPeriod() {
        PasswordResetToken token = PasswordResetToken.create("USER-123", "test@example.com");

        LocalDateTime expectedExpiry = token.getCreatedAt().plusHours(24);
        assertThat(token.getExpiresAt()).isEqualTo(expectedExpiry);
    }

    @Test
    @DisplayName("Should be equal based on token value")
    void shouldBeEqualBasedOnTokenValue() {
        String tokenValue = "same-token";
        PasswordResetToken token1 = PasswordResetToken.reconstitute(
            tokenValue, "USER-1", "test1@example.com",
            LocalDateTime.now(), LocalDateTime.now().plusHours(24), false, null
        );
        PasswordResetToken token2 = PasswordResetToken.reconstitute(
            tokenValue, "USER-2", "test2@example.com",
            LocalDateTime.now(), LocalDateTime.now().plusHours(24), false, null
        );

        assertThat(token1).isEqualTo(token2);
        assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    }

    @Test
    @DisplayName("Should not expose sensitive data in toString")
    void shouldNotExposeSensitiveDataInToString() {
        PasswordResetToken token = PasswordResetToken.create("USER-123", "test@example.com");

        String toString = token.toString();
        assertThat(toString).doesNotContain(token.getToken());
        assertThat(toString).contains("***");
    }
}

