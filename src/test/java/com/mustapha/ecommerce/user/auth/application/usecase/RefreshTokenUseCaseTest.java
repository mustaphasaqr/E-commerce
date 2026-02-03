package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.RefreshTokenCommand;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase Tests")
class RefreshTokenUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginSessionRepository loginSessionRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private RefreshTokenUseCase refreshTokenUseCase;

    private RefreshToken oldToken;
    private RefreshToken newToken;
    private LoginSession newSession;
    private RefreshTokenCommand command;

    @BeforeEach
    void setUp() {
        refreshTokenUseCase = new RefreshTokenUseCase(refreshTokenRepository, loginSessionRepository, eventPublisher, tokenBlacklistService);
        
        oldToken = RefreshToken.create("user-id-123");
        newToken = RefreshToken.create("user-id-123");
        newSession = LoginSession.create("user-id-123", "192.168.1.1", "Mozilla/5.0");

        command = new RefreshTokenCommand(
            UserId.newId(),
            "old-refresh-token",
            "192.168.1.1",
            "Mozilla/5.0"
        );
    }

    @Nested
    @DisplayName("Successful Token Refresh")
    class SuccessfulRefreshTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshToken() {
            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(oldToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);
            when(loginSessionRepository.save(any(LoginSession.class))).thenReturn(newSession);

            RefreshTokenUseCase.RefreshResult result = refreshTokenUseCase.execute(command);

            assertThat(result).isNotNull();
            assertThat(result.getRefreshToken()).isNotNull();
            assertThat(result.getSessionId()).isNotNull();

            verify(refreshTokenRepository, times(1)).findByToken(anyString());
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
            verify(loginSessionRepository, times(1)).save(any(LoginSession.class));
            verify(eventPublisher, times(3)).publish(any());
        }

        @Test
        @DisplayName("Should publish 3 events on token refresh")
        void shouldPublishThreeEvents() {
            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(oldToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);
            when(loginSessionRepository.save(any(LoginSession.class))).thenReturn(newSession);

            refreshTokenUseCase.execute(command);

            verify(eventPublisher, times(3)).publish(any());
        }
    }

    @Nested
    @DisplayName("Validation Failures")
    class ValidationFailureTests {

        @Test
        @DisplayName("Should throw when token not found")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");

            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
            verify(loginSessionRepository, never()).save(any(LoginSession.class));
        }
    }
}
