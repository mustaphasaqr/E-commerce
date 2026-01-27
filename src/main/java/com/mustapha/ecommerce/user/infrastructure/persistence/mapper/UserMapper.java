package com.mustapha.ecommerce.user.infrastructure.persistence.mapper;

import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * User Mapper
 * Pattern: Mapper (Anti-Corruption Layer)
 * 
 * Converts between domain aggregate and JPA entity
 */
@Component
public class UserMapper {

    /**
     * Convert domain aggregate to JPA entity
     */
    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        
        entity.setId(user.getId().toString());
        entity.setUsername(user.getUsername().getValue());
        entity.setEmail(user.getEmail().getValue());
        entity.setHashedPassword(user.getPassword().getHashedValue());
        entity.setRole(mapRoleToEntity(user.getRole()));
        entity.setStatus(mapStatusToEntity(user.getStatus()));
        entity.setEmailVerified(user.isEmailVerified());
        entity.setBlockReason(user.getBlockReason());
        entity.setDeleted(user.isDeleted());
        entity.setDeletedAt(user.getDeletedAt());
        entity.setDeletionReason(user.getDeletionReason());
        entity.setTermsAccepted(user.isTermsAccepted());
        entity.setTermsAcceptedAt(user.getTermsAcceptedAt());
        entity.setTermsVersion(user.getTermsVersion());
        entity.setMarketingConsentGiven(user.isMarketingConsentGiven());
        entity.setMarketingConsentDate(user.getMarketingConsentDate());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getLastModifiedAt());
        
        return entity;
    }

    /**
     * Update existing JPA entity with domain aggregate data
     * Pattern: Merge existing Hibernate-managed entity to avoid detached entity conflicts
     */
    public UserJpaEntity updateEntity(UserJpaEntity entity, User user) {
        // Update all fields except ID (immutable)
        entity.setUsername(user.getUsername().getValue());
        entity.setEmail(user.getEmail().getValue());
        entity.setHashedPassword(user.getPassword().getHashedValue());
        entity.setRole(mapRoleToEntity(user.getRole()));
        entity.setStatus(mapStatusToEntity(user.getStatus()));
        entity.setEmailVerified(user.isEmailVerified());
        entity.setBlockReason(user.getBlockReason());
        entity.setDeleted(user.isDeleted());
        entity.setDeletedAt(user.getDeletedAt());
        entity.setDeletionReason(user.getDeletionReason());
        entity.setTermsAccepted(user.isTermsAccepted());
        entity.setTermsAcceptedAt(user.getTermsAcceptedAt());
        entity.setTermsVersion(user.getTermsVersion());
        entity.setMarketingConsentGiven(user.isMarketingConsentGiven());
        entity.setMarketingConsentDate(user.getMarketingConsentDate());
        entity.setUpdatedAt(user.getLastModifiedAt());
        
        return entity;
    }

    /**
     * Convert JPA entity to domain aggregate
     */
    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
            UserId.of(entity.getId()),
            Username.of(entity.getUsername()),
            Email.of(entity.getEmail()),
            Password.fromHashed(entity.getHashedPassword()),
            mapRoleToDomain(entity.getRole()),
            mapStatusToDomain(entity.getStatus()),
            entity.isEmailVerified(),
            entity.getBlockReason(),
            entity.isDeleted(),
            entity.getDeletedAt(),
            entity.getDeletionReason(),
            entity.isTermsAccepted(),
            entity.getTermsAcceptedAt(),
            entity.getTermsVersion(),
            entity.isMarketingConsentGiven(),
            entity.getMarketingConsentDate(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private UserJpaEntity.RoleType mapRoleToEntity(Role role) {
        return UserJpaEntity.RoleType.valueOf(role.name());
    }

    private Role mapRoleToDomain(UserJpaEntity.RoleType roleType) {
        return Role.valueOf(roleType.name());
    }

    private UserJpaEntity.StatusType mapStatusToEntity(User.UserStatus status) {
        return UserJpaEntity.StatusType.valueOf(status.name());
    }

    private User.UserStatus mapStatusToDomain(UserJpaEntity.StatusType statusType) {
        return User.UserStatus.valueOf(statusType.name());
    }
}
