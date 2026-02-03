package com.mustapha.ecommerce.user.auth.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

    private final Map<String, RefreshToken> tokens = new ConcurrentHashMap<>();

    @Override
    public RefreshToken save(RefreshToken token) {
        tokens.put(token.getTokenValue(), token);
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String tokenValue) {
        return Optional.ofNullable(tokens.get(tokenValue));
    }

    @Override
    public void delete(String tokenValue) {
        tokens.remove(tokenValue);
    }

    @Override
    public void deleteAllByUserId(UserId userId) {
        tokens.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId.getValue().toString()));
    }
}
