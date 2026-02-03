package com.mustapha.ecommerce.user.auth.domain.repository;

import com.mustapha.ecommerce.user.auth.domain.model.EmailVerificationToken;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.Optional;

public interface EmailVerificationTokenRepository {
    EmailVerificationToken save(EmailVerificationToken token);
    Optional<EmailVerificationToken> findByToken(String tokenValue);
    void delete(String tokenValue);
    void deleteAllByUserId(UserId userId);
}
