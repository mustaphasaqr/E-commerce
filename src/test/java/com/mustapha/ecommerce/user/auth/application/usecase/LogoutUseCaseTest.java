package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.LogoutCommand;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("LogoutUseCase Tests")
class LogoutUseCaseTest {

    @Mock
    private LoginSessionRepository loginSessionRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private LogoutUseCase logoutUseCase;

    private LoginSession mockSession;
    private LogoutCommand command;

    @BeforeEach
    void setUp() {
        logoutUseCase = new LogoutUseCase(loginSessionRepository, eventPublisher, tokenBlacklistService, 3600000L);
        
        mockSession = LoginSession.create(
            "user-id-123",
            "192.168.1.1",
            "Mozilla/5.0"
        );

        command = new LogoutCommand(
            UserId.newId(),
            "session-id-123",
            "mock-jwt-token"
        );
    }

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogout() {
        when(loginSessionRepository.findBySessionId(anyString())).thenReturn(Optional.of(mockSession));
        when(loginSessionRepository.save(any(LoginSession.class))).thenReturn(mockSession);

        logoutUseCase.execute(command);

        verify(loginSessionRepository, times(1)).findBySessionId(anyString());
        verify(loginSessionRepository, times(1)).save(any(LoginSession.class));
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("Should throw when session not found")
    void shouldThrowWhenSessionNotFound() {
        when(loginSessionRepository.findBySessionId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logoutUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Session not found");

        verify(loginSessionRepository, never()).save(any(LoginSession.class));
        verify(eventPublisher, never()).publish(any());
    }
}
