package com.mustapha.ecommerce.user.auth.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@EntityScan("com.mustapha.ecommerce")
@Import(InMemoryLoginSessionRepository.class)
@DisplayName("LoginSessionRepository Integration Tests")
class LoginSessionRepositoryTest {

    @Autowired
    private LoginSessionRepository sessionRepository;

    private LoginSession testSession;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UserId.newId().getValue().toString();
        testSession = LoginSession.create(
            testUserId,
            "192.168.1.1",
            "Mozilla/5.0"
        );
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save login session successfully")
        void shouldSaveSession() {
            LoginSession saved = sessionRepository.save(testSession);

            assertThat(saved).isNotNull();
            assertThat(saved.getSessionId()).isNotNull();
            assertThat(saved.getUserId()).isEqualTo(testUserId);
            assertThat(saved.getIpAddress()).isEqualTo("192.168.1.1");
            assertThat(saved.getUserAgent()).isEqualTo("Mozilla/5.0");
        }

        @Test
        @DisplayName("Should update existing session")
        void shouldUpdateSession() {
            LoginSession saved = sessionRepository.save(testSession);
            saved.invalidate();

            LoginSession updated = sessionRepository.save(saved);

            assertThat(updated.getSessionId()).isEqualTo(saved.getSessionId());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find session by session ID")
        void shouldFindBySessionId() {
            LoginSession saved = sessionRepository.save(testSession);

            Optional<LoginSession> found = sessionRepository.findBySessionId(saved.getSessionId());

            assertThat(found).isPresent();
            assertThat(found.get().getSessionId()).isEqualTo(saved.getSessionId());
            assertThat(found.get().getUserId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("Should return empty when session not found")
        void shouldReturnEmptyWhenSessionNotFound() {
            Optional<LoginSession> found = sessionRepository.findBySessionId("nonexistent-session-id");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find active sessions by user ID")
        void shouldFindActiveSessionsByUserId() {
            LoginSession session1 = LoginSession.create(testUserId, "192.168.1.1", "Chrome");
            LoginSession session2 = LoginSession.create(testUserId, "192.168.1.2", "Firefox");
            
            sessionRepository.save(session1);
            sessionRepository.save(session2);

            List<LoginSession> sessions = sessionRepository.findActiveSessionsByUserId(UserId.of(testUserId));

            assertThat(sessions).hasSize(2);
            assertThat(sessions).allMatch(s -> s.getUserId().equals(testUserId));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete session by session ID")
        void shouldDeleteBySessionId() {
            LoginSession saved = sessionRepository.save(testSession);

            sessionRepository.delete(saved.getSessionId());

            Optional<LoginSession> found = sessionRepository.findBySessionId(saved.getSessionId());
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should delete all sessions for user")
        void shouldDeleteAllByUserId() {
            LoginSession session1 = LoginSession.create(testUserId, "192.168.1.1", "Chrome");
            LoginSession session2 = LoginSession.create(testUserId, "192.168.1.2", "Firefox");

            sessionRepository.save(session1);
            sessionRepository.save(session2);

            sessionRepository.deleteAllByUserId(UserId.of(testUserId));

            List<LoginSession> sessions = sessionRepository.findActiveSessionsByUserId(UserId.of(testUserId));
            assertThat(sessions).isEmpty();
        }

        @Test
        @DisplayName("Should delete all sessions except current")
        void shouldDeleteAllByUserIdExceptCurrent() {
            LoginSession session1 = LoginSession.create(testUserId, "192.168.1.1", "Chrome");
            LoginSession session2 = LoginSession.create(testUserId, "192.168.1.2", "Firefox");
            LoginSession session3 = LoginSession.create(testUserId, "192.168.1.3", "Safari");

            sessionRepository.save(session1);
            sessionRepository.save(session2);
            sessionRepository.save(session3);

            sessionRepository.deleteAllByUserIdExcept(UserId.of(testUserId), session2.getSessionId());

            Optional<LoginSession> found1 = sessionRepository.findBySessionId(session1.getSessionId());
            Optional<LoginSession> found2 = sessionRepository.findBySessionId(session2.getSessionId());
            Optional<LoginSession> found3 = sessionRepository.findBySessionId(session3.getSessionId());

            assertThat(found1).isEmpty();
            assertThat(found2).isPresent();
            assertThat(found3).isEmpty();
        }
    }
}
