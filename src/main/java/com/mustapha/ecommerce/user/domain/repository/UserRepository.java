package com.mustapha.ecommerce.user.domain.repository;

import java.util.Optional;

import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * User Repository Interface (Port in Hexagonal Architecture)
 * 
 * Defines data access operations for User aggregate.
 * Implementation will be in infrastructure layer (JPA, MongoDB, etc.)
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    Optional<User> findByUsername(Username username);
    boolean existsByEmail(Email email);
    boolean existsByUsername(Username username);
    void delete(UserId id);
    
    // Admin query operations
    Page<User> findAll(Pageable pageable);
    Page<User> search(String email, String username, User.UserStatus status, Role role, Pageable pageable);
}
