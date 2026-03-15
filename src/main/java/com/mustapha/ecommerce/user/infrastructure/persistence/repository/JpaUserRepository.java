package com.mustapha.ecommerce.user.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import com.mustapha.ecommerce.user.infrastructure.persistence.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA User Repository Implementation
 * Responsibility: Implement UserRepository interface using JPA
 * Pattern: Repository, Adapter
 */
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataRepository;
    private final UserMapper mapper;

    public JpaUserRepository(SpringDataUserRepository springDataRepository, UserMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        // Check if entity already exists to avoid detached entity issues
        Optional<UserJpaEntity> existingEntity = springDataRepository.findById(user.getId().toString());
        
        UserJpaEntity entity;
        if (existingEntity.isPresent()) {
            // Update existing entity (merge pattern)
            entity = mapper.updateEntity(existingEntity.get(), user);
        } else {
            // Create new entity
            entity = mapper.toEntity(user);
        }
        
        UserJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataRepository.findById(id.toString())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataRepository.findByEmail(email.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return springDataRepository.findByUsername(username.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataRepository.existsByEmail(email.getValue());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return springDataRepository.existsByUsername(username.getValue());
    }

    @Override
    public void delete(UserId id) {
        springDataRepository.deleteById(id.toString());
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return springDataRepository.findByDeletedFalse(pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<User> search(String email, String username, User.UserStatus status, Role role, Pageable pageable) {
        UserJpaEntity.StatusType statusType = status != null ? UserJpaEntity.StatusType.valueOf(status.name()) : null;
        UserJpaEntity.RoleType roleType = role != null ? UserJpaEntity.RoleType.valueOf(role.name()) : null;
        
        return springDataRepository.search(email, username, statusType, roleType, pageable)
            .map(mapper::toDomain);
    }
}
