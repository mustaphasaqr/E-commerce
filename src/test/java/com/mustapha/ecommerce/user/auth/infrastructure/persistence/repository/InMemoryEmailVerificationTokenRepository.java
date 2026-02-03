package com.mustapha.ecommerce.user.auth.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.auth.domain.model.EmailVerificationToken;
import com.mustapha.ecommerce.user.auth.domain.repository.EmailVerificationTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEmailVerificationTokenRepository implements EmailVerificationTokenRepository {

    private final Map<String, EmailVerificationToken> tokens = new ConcurrentHashMap<>();

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        tokens.put(token.getToken(), token);
        return token;
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        EmailVerificationToken found = tokens.get(token);
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
