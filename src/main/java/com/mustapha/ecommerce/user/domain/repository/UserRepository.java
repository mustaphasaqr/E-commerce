package com.mustapha.ecommerce.user.domain.repository;

import java.util.Optional;

import com.mustapha.ecommerce.user.domain.model.User;

/**
 * User Repository Interface
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    void delete(String id);
}
