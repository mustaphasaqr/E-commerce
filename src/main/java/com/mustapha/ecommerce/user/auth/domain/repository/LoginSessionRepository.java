package com.mustapha.ecommerce.user.auth.domain.repository;

import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * LoginSession Repository Interface (Port in Hexagonal Architecture)
 * 
 * Defines data access operations for LoginSession aggregate.
 * Implementation will be in infrastructure layer.
 */
public interface LoginSessionRepository {
    LoginSession save(LoginSession session);
    Optional<LoginSession> findBySessionId(String sessionId);
    List<LoginSession> findActiveSessionsByUserId(UserId userId);
    void delete(String sessionId);
    void deleteAllByUserId(UserId userId);
    void deleteAllByUserIdExcept(UserId userId, String currentSessionId); // Logout all devices except current
}
