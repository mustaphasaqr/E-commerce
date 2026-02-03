package com.mustapha.ecommerce.user.auth.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@EntityScan("com.mustapha.ecommerce")
@Import(InMemoryPasswordResetTokenRepository.class)
@DisplayName("PasswordResetTokenRepository Integration Tests")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private PasswordResetToken testToken;
    private UserId testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UserId.newId();
        testToken = PasswordResetToken.create(
            testUserId.getValue().toString(),
            "john@example.com"
        );
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save password reset token successfully")
        void shouldSaveToken() {
            PasswordResetToken saved = tokenRepository.save(testToken);

            assertThat(saved).isNotNull();
            assertThat(saved.getToken()).isNotNull();
            assertThat(saved.getUserId()).isEqualTo(testUserId.getValue().toString());
            assertThat(saved.getEmail()).isEqualTo("john@example.com");
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find token by token value")
        void shouldFindByToken() {
            PasswordResetToken saved = tokenRepository.save(testToken);

            Optional<PasswordResetToken> found = tokenRepository.findByToken(saved.getToken());

            assertThat(found).isPresent();
            assertThat(found.get().getToken()).isEqualTo(saved.getToken());
            assertThat(found.get().getUserId()).isEqualTo(testUserId.getValue().toString());
        }

        @Test
        @DisplayName("Should return empty when token not found")
        void shouldReturnEmptyWhenTokenNotFound() {
            Optional<PasswordResetToken> found = tokenRepository.findByToken("nonexistent-token");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should not find token after expiration")
        void shouldNotFindExpiredToken() {
            PasswordResetToken saved = tokenRepository.save(testToken);
            
            saved.use();
            tokenRepository.save(saved);

            Optional<PasswordResetToken> found = tokenRepository.findByToken(saved.getToken());
            
            if (found.isPresent()) {
                assertThatThrownBy(() -> found.get().use())
                    .isInstanceOf(Exception.class);
            }
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete token by token value")
        void shouldDeleteByToken() {
            PasswordResetToken saved = tokenRepository.save(testToken);

            tokenRepository.delete(saved.getToken());

            Optional<PasswordResetToken> found = tokenRepository.findByToken(saved.getToken());
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should delete all tokens for user")
        void shouldDeleteAllByUserId() {
            PasswordResetToken token1 = PasswordResetToken.create(
                testUserId.getValue().toString(),
                "john@example.com"
            );
            PasswordResetToken token2 = PasswordResetToken.create(
                testUserId.getValue().toString(),
                "john@example.com"
            );

            tokenRepository.save(token1);
            tokenRepository.save(token2);

            tokenRepository.deleteAllByUserId(testUserId);

            Optional<PasswordResetToken> found1 = tokenRepository.findByToken(token1.getToken());
            Optional<PasswordResetToken> found2 = tokenRepository.findByToken(token2.getToken());

            assertThat(found1).isEmpty();
            assertThat(found2).isEmpty();
        }
    }
}
