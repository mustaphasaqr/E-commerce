package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.auth.application.command.LogoutAllDevicesCommand;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutAllDevicesUseCase Tests")
class LogoutAllDevicesUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginSessionRepository loginSessionRepository;

    @InjectMocks
    private LogoutAllDevicesUseCase logoutAllDevicesUseCase;

    private LogoutAllDevicesCommand command;

    @BeforeEach
    void setUp() {
        command = new LogoutAllDevicesCommand(
            UserId.newId(),
            "current-session-id"
        );
    }

    @Test
    @DisplayName("Should logout all devices successfully")
    void shouldLogoutAllDevices() {
        logoutAllDevicesUseCase.execute(command);

        verify(refreshTokenRepository, times(1)).deleteAllByUserId(any(UserId.class));
        verify(loginSessionRepository, times(1)).deleteAllByUserIdExcept(any(UserId.class), anyString());
    }

    @Test
    @DisplayName("Should preserve current session")
    void shouldPreserveCurrentSession() {
        logoutAllDevicesUseCase.execute(command);

        verify(loginSessionRepository, times(1)).deleteAllByUserIdExcept(
            any(UserId.class),
            eq("current-session-id")
        );
    }
}
