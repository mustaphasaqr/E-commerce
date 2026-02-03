package com.mustapha.ecommerce.user.auth.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryLoginSessionRepository implements LoginSessionRepository {

    private final Map<String, LoginSession> sessions = new ConcurrentHashMap<>();

    @Override
    public LoginSession save(LoginSession session) {
        sessions.put(session.getSessionId(), session);
        return session;
    }

    @Override
    public Optional<LoginSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<LoginSession> findActiveSessionsByUserId(UserId userId) {
        return sessions.values().stream()
            .filter(session -> session.getUserId().equals(userId.getValue().toString()))
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public void deleteAllByUserId(UserId userId) {
        sessions.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId.getValue().toString()));
    }

    @Override
    public void deleteAllByUserIdExcept(UserId userId, String currentSessionId) {
        sessions.entrySet().removeIf(entry -> 
            entry.getValue().getUserId().equals(userId.getValue().toString()) && 
            !entry.getKey().equals(currentSessionId)
        );
    }
}
