package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.auth.application.command.LogoutAllDevicesCommand;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logout All Devices Use Case
 * Responsibility: Revoke all sessions and tokens except current
 * Pattern: Use Case (Application Service)
 * 
 * Use cases:
 * - User suspects account compromise
 * - User wants to force logout from all devices
 * - Security feature for password change
 */
@Component
public class LogoutAllDevicesUseCase {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginSessionRepository loginSessionRepository;

    public LogoutAllDevicesUseCase(RefreshTokenRepository refreshTokenRepository,
                                   LoginSessionRepository loginSessionRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginSessionRepository = loginSessionRepository;
    }
    
    @Transactional
    public void execute(LogoutAllDevicesCommand command) {
        // Revoke all refresh tokens for this user
        refreshTokenRepository.deleteAllByUserId(command.getUserId());
        
        // Invalidate all sessions except current
        loginSessionRepository.deleteAllByUserIdExcept(
            command.getUserId(), 
            command.getCurrentSessionId()
        );
        
        // TODO: Publish RefreshTokenRevokedEvent with count
    }
}
