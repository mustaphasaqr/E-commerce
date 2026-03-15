package com.mustapha.ecommerce.user.infrastructure.bootstrap;

import com.mustapha.ecommerce.user.domain.model.valueobject.PasswordHasher;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import com.mustapha.ecommerce.user.infrastructure.persistence.repository.SpringDataUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates or repairs the OWNER account on startup without DB migration scripts.
 *
 * Enabled only when app.owner.bootstrap.enabled=true.
 */
@Component
public class OwnerAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OwnerAccountInitializer.class);

    private final SpringDataUserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Value("${app.owner.bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${app.owner.bootstrap.email:owner@mecommerce.com}")
    private String ownerEmail;

    @Value("${app.owner.bootstrap.username:owner}")
    private String ownerUsername;

    @Value("${app.owner.bootstrap.password:}")
    private String ownerPassword;

    public OwnerAccountInitializer(SpringDataUserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        if (ownerPassword == null || ownerPassword.isBlank()) {
            log.warn("OWNER bootstrap is enabled but app.owner.bootstrap.password is empty. Skipping owner initialization.");
            return;
        }

        String normalizedEmail = ownerEmail.toLowerCase();
        String normalizedUsername = ownerUsername.toLowerCase();

        try {
            Optional<UserJpaEntity> existingByEmail = userRepository.findByEmail(normalizedEmail);
            if (existingByEmail.isPresent()) {
                UserJpaEntity existing = existingByEmail.get();

                boolean alreadyHealthy = normalizedUsername.equals(existing.getUsername())
                    && UserJpaEntity.RoleType.OWNER == existing.getRole()
                    && UserJpaEntity.StatusType.ACTIVE == existing.getStatus()
                    && existing.isEmailVerified()
                    && !existing.isDeleted()
                    && passwordHasher.matches(ownerPassword, existing.getHashedPassword());

                if (alreadyHealthy) {
                    log.info("OWNER bootstrap skipped: account already initialized and healthy for email={}", normalizedEmail);
                    return;
                }

                // Repair partially incorrect owner state once, then future startups will skip.
                existing.setUsername(normalizedUsername);
                existing.setEmail(normalizedEmail);
                existing.setRole(UserJpaEntity.RoleType.OWNER);
                existing.setStatus(UserJpaEntity.StatusType.ACTIVE);
                existing.setEmailVerified(true);
                existing.setBlockReason(null);
                existing.setDeleted(false);
                existing.setDeletedAt(null);
                existing.setDeletionReason(null);
                existing.setTermsAccepted(true);
                if (existing.getTermsAcceptedAt() == null) {
                    existing.setTermsAcceptedAt(LocalDateTime.now());
                }
                if (existing.getTermsVersion() == null || existing.getTermsVersion().isBlank()) {
                    existing.setTermsVersion("v1.0");
                }
                existing.setMarketingConsentGiven(false);
                existing.setMarketingConsentDate(null);
                if (!passwordHasher.matches(ownerPassword, existing.getHashedPassword())) {
                    existing.setHashedPassword(passwordHasher.hash(ownerPassword));
                }
                if (existing.getVersion() == null || existing.getVersion() < 1L) {
                    existing.setVersion(1L);
                }
                existing.setUpdatedAt(LocalDateTime.now());
                existing.setUpdatedBy("SYSTEM");

                userRepository.save(existing);
                log.info("OWNER account repaired successfully for email={}", normalizedEmail);
                return;
            }

            UserJpaEntity owner = new UserJpaEntity();
            owner.setId(UUID.randomUUID().toString());
            owner.setUsername(normalizedUsername);
            owner.setEmail(normalizedEmail);
            owner.setHashedPassword(passwordHasher.hash(ownerPassword));
            owner.setRole(UserJpaEntity.RoleType.OWNER);
            owner.setStatus(UserJpaEntity.StatusType.ACTIVE);
            owner.setEmailVerified(true);
            owner.setBlockReason(null);
            owner.setDeleted(false);
            owner.setDeletedAt(null);
            owner.setDeletionReason(null);
            owner.setTermsAccepted(true);
            owner.setTermsAcceptedAt(LocalDateTime.now());
            owner.setTermsVersion("v1.0");
            owner.setMarketingConsentGiven(false);
            owner.setMarketingConsentDate(null);
            owner.setVersion(1L);
            owner.setCreatedAt(LocalDateTime.now());
            owner.setCreatedBy("SYSTEM");
            owner.setUpdatedAt(LocalDateTime.now());
            owner.setUpdatedBy("SYSTEM");

            userRepository.save(owner);
            log.info("OWNER account created successfully for email={}", normalizedEmail);
        } catch (Exception ex) {
            log.error("OWNER bootstrap failed for email={} but startup will continue.", normalizedEmail, ex);
        }
    }
}
