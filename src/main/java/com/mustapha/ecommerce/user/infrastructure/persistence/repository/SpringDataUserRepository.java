package com.mustapha.ecommerce.user.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for User
 * Pattern: Spring Data JPA Repository
 * 
 * Provides CRUD operations for UserJpaEntity
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {

    /**
     * List only non-deleted users (soft-delete aware listing).
     */
    Page<UserJpaEntity> findByDeletedFalse(Pageable pageable);

    /**
     * Find user by email
     */
    Optional<UserJpaEntity> findByEmail(String email);

    /**
     * Find user by username
     */
    Optional<UserJpaEntity> findByUsername(String username);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by username
     */
    boolean existsByUsername(String username);
    
    /**
     * Search users by criteria (admin only)
     */
    @Query("SELECT u FROM UserJpaEntity u WHERE u.deleted = false AND " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:role IS NULL OR u.role = :role)")
    Page<UserJpaEntity> search(
        @Param("email") String email,
        @Param("username") String username,
        @Param("status") UserJpaEntity.StatusType status,
        @Param("role") UserJpaEntity.RoleType role,
        Pageable pageable
    );
}

