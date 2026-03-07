package com.mustapha.ecommerce.config;

import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.repository.SpringDataProductRepository;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import com.mustapha.ecommerce.user.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test Data Seeder
 * 
 * Runs on application startup for test profile ONLY
 * Creates essential test entities:
 * - Test products (for product API tests)
 * - Test users (for auth/analytics tests)
 * 
 * Phase: Runs BEFORE any tests execute
 * Cleanup: Automatic via @DirtiesContext on each test
 */
@Slf4j
@Configuration
@Profile("test")
public class TestDataSeeder {

    @Bean
    public CommandLineRunner seedTestData(
            SpringDataProductRepository springDataProductRepository,
            SpringDataUserRepository springDataUserRepository,
            BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            seedProducts(springDataProductRepository);
            seedUsers(springDataUserRepository, passwordEncoder);
        };
    }

    /**
     * Seed test products
     * 
     * Creates multiple products for various tests:
     * - UUID 00000000-0000-0000-0000-000000000001: Default test product (used by API/Analytics/Cache tests)
     * - Additional products for price range, filtering, pagination tests
     */
    private void seedProducts(SpringDataProductRepository springDataProductRepository) {
        // Check if already seeded (prevent duplicates on app restart)
        String testProductId = "00000000-0000-0000-0000-000000000001";
        if (springDataProductRepository.findById(testProductId).isPresent()) {
            log.info("✅ Test products already seeded, skipping");
            return;
        }

        log.info("🌱 Seeding test products...");

        // Product 1: Default test product (used by most tests)
        ProductJpaEntity product1 = new ProductJpaEntity();
        product1.setId(testProductId);
        product1.setName("Test Product - Default");
        product1.setDescription("Primary test product used by API tests, caching tests, and analytics");
        product1.setPrice(new BigDecimal("99.99"));
        product1.setTotalStock(100);
        product1.setAvailableStock(100);
        product1.setReservedStock(0);
        product1.setActive(true);
        product1.setVisible(true);
        product1.setAvailableForPurchase(true);
        product1.setDiscontinued(false);
        product1.setCreatedAt(LocalDateTime.now());
        product1.setUpdatedAt(LocalDateTime.now());
        springDataProductRepository.save(product1);
        log.info("✅ Created product 1: {}", testProductId);

        // Product 2: For price range tests
        ProductJpaEntity product2 = new ProductJpaEntity();
        product2.setId(UUID.randomUUID().toString());
        product2.setName("Test Product - Cheap");
        product2.setDescription("Low-price test product");
        product2.setPrice(new BigDecimal("9.99"));
        product2.setTotalStock(50);
        product2.setAvailableStock(50);
        product2.setReservedStock(0);
        product2.setActive(true);
        product2.setVisible(true);
        product2.setAvailableForPurchase(true);
        product2.setDiscontinued(false);
        product2.setCreatedAt(LocalDateTime.now());
        product2.setUpdatedAt(LocalDateTime.now());
        springDataProductRepository.save(product2);
        log.info("✅ Created product 2: Cheap variant");

        // Product 3: For high-price tests
        ProductJpaEntity product3 = new ProductJpaEntity();
        product3.setId(UUID.randomUUID().toString());
        product3.setName("Test Product - Premium");
        product3.setDescription("High-price test product");
        product3.setPrice(new BigDecimal("999.99"));
        product3.setTotalStock(10);
        product3.setAvailableStock(10);
        product3.setReservedStock(0);
        product3.setActive(true);
        product3.setVisible(true);
        product3.setAvailableForPurchase(true);
        product3.setDiscontinued(false);
        product3.setCreatedAt(LocalDateTime.now());
        product3.setUpdatedAt(LocalDateTime.now());
        springDataProductRepository.save(product3);
        log.info("✅ Created product 3: Premium variant");

        // Product 4: For out-of-stock tests
        ProductJpaEntity product4 = new ProductJpaEntity();
        product4.setId(UUID.randomUUID().toString());
        product4.setName("Test Product - Out of Stock");
        product4.setDescription("Out of stock test product");
        product4.setPrice(new BigDecimal("49.99"));
        product4.setTotalStock(0);
        product4.setVisible(true);
        product4.setAvailableForPurchase(true);
        product4.setDiscontinued(false);
        product4.setAvailableStock(0);
        product4.setReservedStock(0);
        product4.setActive(true);
        product4.setCreatedAt(LocalDateTime.now());
        product4.setUpdatedAt(LocalDateTime.now());
        springDataProductRepository.save(product4);
        log.info("✅ Created product 4: Out of stock variant");

        log.info("🌱 Test products seeded successfully (4 products)");
    }

    /**
     * Seed test users
     * 
     * Creates test users for auth/analytics tests:
     * - OWNER role: Can view analytics
     * - EMPLOYEE role: Can manage products
     * - CUSTOMER role: Can buy products
     */
    private void seedUsers(SpringDataUserRepository springDataUserRepository, BCryptPasswordEncoder passwordEncoder) {
        // Check if already seeded
        if (springDataUserRepository.findByEmail("owner@test.local").isPresent()) {
            log.info("✅ Test users already seeded, skipping");
            return;
        }

        log.info("🌱 Seeding test users...");

        // Owner user (for analytics)
        UserJpaEntity owner = new UserJpaEntity();
        owner.setId(UUID.randomUUID().toString());
        owner.setEmail("owner@test.local");
        owner.setUsername("test-owner");
        owner.setHashedPassword(passwordEncoder.encode("password123"));
        owner.setRole(UserJpaEntity.RoleType.OWNER);
        owner.setStatus(UserJpaEntity.StatusType.ACTIVE);
        owner.setEmailVerified(true);
        owner.setTermsAccepted(true);
        owner.setCreatedAt(LocalDateTime.now());
        springDataUserRepository.save(owner);
        log.info("✅ Created OWNER user: owner@test.local");

        // Employee user (for product management)
        UserJpaEntity employee = new UserJpaEntity();
        employee.setId(UUID.randomUUID().toString());
        employee.setEmail("employee@test.local");
        employee.setUsername("test-employee");
        employee.setHashedPassword(passwordEncoder.encode("password123"));
        employee.setRole(UserJpaEntity.RoleType.EMPLOYEE);
        employee.setStatus(UserJpaEntity.StatusType.ACTIVE);
        employee.setEmailVerified(true);
        employee.setTermsAccepted(true);
        employee.setCreatedAt(LocalDateTime.now());
        springDataUserRepository.save(employee);
        log.info("✅ Created EMPLOYEE user: employee@test.local");

        // Customer user (for shopping)
        UserJpaEntity customer = new UserJpaEntity();
        customer.setId(UUID.randomUUID().toString());
        customer.setEmail("customer@test.local");
        customer.setUsername("test-customer");
        customer.setHashedPassword(passwordEncoder.encode("password123"));
        customer.setRole(UserJpaEntity.RoleType.CUSTOMER);
        customer.setStatus(UserJpaEntity.StatusType.ACTIVE);
        customer.setEmailVerified(true);
        customer.setTermsAccepted(true);
        customer.setCreatedAt(LocalDateTime.now());
        springDataUserRepository.save(customer);
        log.info("✅ Created CUSTOMER user: customer@test.local");

        log.info("🌱 Test users seeded successfully (3 users)");
    }
}
