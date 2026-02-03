package com.mustapha.ecommerce.user.auth.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final Map<String, PasswordResetToken> tokens = new ConcurrentHashMap<>();

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        tokens.put(token.getToken(), token);
        return token;
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String tokenValue) {
        PasswordResetToken found = tokens.get(tokenValue);
        if (found != null && !found.isUsed()) {
            return Optional.of(found);
        }
        return Optional.empty();
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
