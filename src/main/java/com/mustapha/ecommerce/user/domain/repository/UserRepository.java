package com.mustapha.ecommerce.user.domain.repository;

import java.util.Optional;

import com.mustapha.ecommerce.user.domain.model.User;

/**
 * User Repository Interface (Port in Hexagonal Architecture)
 * 
 * Defines data access operations for User aggregate.
 * Implementation will be in infrastructure layer (JPA, MongoDB, etc.)
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    void delete(String id);
}
