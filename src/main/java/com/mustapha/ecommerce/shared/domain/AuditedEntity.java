package com.mustapha.ecommerce.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Audited Entity Base Class
 * 
 * Provides audit fields for all entities that extend it.
 * Automatically tracks:
 * - Who created the entity (createdBy)
 * - When it was created (createdAt)
 * - Who last modified it (updatedBy)
 * - When it was last modified (updatedAt)
 * 
 * Pattern: Base Entity, JPA Auditing
 * Layer: SHARED / INFRASTRUCTURE
 * 
 * Compliance Benefits:
 * - GDPR Article 30: Record of processing activities
 * - SOX compliance: Financial transaction tracking
 * - Fraud prevention: Track suspicious modifications
 * - Customer disputes: "Who changed my order?"
 * - Security auditing: Track admin actions
 * 
 * Usage:
 * Make your JPA entities extend this class:
 * <pre>
 * {@code
 * @Entity
 * public class ProductJpaEntity extends AuditedEntity {
 *     // your fields...
 * }
 * }
 * </pre>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditedEntity {
    
    @CreatedBy
    @Column(name = "created_by", nullable = true, updatable = false, length = 100)
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedBy
    @Column(name = "updated_by", nullable = true, length = 100)
    private String updatedBy;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
    
    // ========== Getters ==========
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // ========== Setters (for testing/migration) ==========
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
