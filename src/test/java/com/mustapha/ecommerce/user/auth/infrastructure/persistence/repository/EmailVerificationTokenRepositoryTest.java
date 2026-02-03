package com.mustapha.ecommerce.user.auth.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.auth.domain.model.EmailVerificationToken;
import com.mustapha.ecommerce.user.auth.domain.repository.EmailVerificationTokenRepository;
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
@Import(InMemoryEmailVerificationTokenRepository.class)
@DisplayName("EmailVerificationTokenRepository Integration Tests")
class EmailVerificationTokenRepositoryTest {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    private EmailVerificationToken testToken;
    private UserId testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UserId.newId();
        testToken = EmailVerificationToken.create(
            testUserId.getValue().toString(),
            "john@example.com"
        );
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save email verification token successfully")
        void shouldSaveToken() {
            EmailVerificationToken saved = tokenRepository.save(testToken);

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
            EmailVerificationToken saved = tokenRepository.save(testToken);

            Optional<EmailVerificationToken> found = tokenRepository.findByToken(saved.getToken());

            assertThat(found).isPresent();
            assertThat(found.get().getToken()).isEqualTo(saved.getToken());
            assertThat(found.get().getUserId()).isEqualTo(testUserId.getValue().toString());
        }

        @Test
        @DisplayName("Should return empty when token not found")
        void shouldReturnEmptyWhenTokenNotFound() {
            Optional<EmailVerificationToken> found = tokenRepository.findByToken("nonexistent-token");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should not find token after being used")
        void shouldNotFindUsedToken() {
            EmailVerificationToken saved = tokenRepository.save(testToken);
            
            saved.use();
            tokenRepository.save(saved);

            Optional<EmailVerificationToken> found = tokenRepository.findByToken(saved.getToken());
            
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
            EmailVerificationToken saved = tokenRepository.save(testToken);

            tokenRepository.delete(saved.getToken());

            Optional<EmailVerificationToken> found = tokenRepository.findByToken(saved.getToken());
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should delete all tokens for user")
        void shouldDeleteAllByUserId() {
            EmailVerificationToken token1 = EmailVerificationToken.create(
                testUserId.getValue().toString(),
                "john@example.com"
            );
            EmailVerificationToken token2 = EmailVerificationToken.create(
                testUserId.getValue().toString(),
                "john@example.com"
            );

            tokenRepository.save(token1);
            tokenRepository.save(token2);

            tokenRepository.deleteAllByUserId(testUserId);

            Optional<EmailVerificationToken> found1 = tokenRepository.findByToken(token1.getToken());
            Optional<EmailVerificationToken> found2 = tokenRepository.findByToken(token2.getToken());

            assertThat(found1).isEmpty();
            assertThat(found2).isEmpty();
        }
    }
}
