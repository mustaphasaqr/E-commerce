package com.mustapha.ecommerce.analytics.infrastructure.persistence.repository;

import com.mustapha.ecommerce.analytics.domain.model.*;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MinimalAnalyticsTest {

    @Autowired
    private JpaAnalyticsRepository analyticsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Minimal test - create order and query")
    void testMinimal() {
        System.out.println("=== MINIMAL TEST START ===");
        
        LocalDate startDate = LocalDate.now().minusDays(60);
        LocalDate endDate = LocalDate.now().minusDays(30);
        
        // Create user
        UserJpaEntity user = new UserJpaEntity();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setHashedPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole(UserJpaEntity.RoleType.CUSTOMER);
        user.setStatus(UserJpaEntity.StatusType.ACTIVE);
        user.setTermsAccepted(true);
        entityManager.persist(user);
        
        // Create products
        ProductJpaEntity laptop = new ProductJpaEntity();
        laptop.setId("PROD-001");
        laptop.setSku("LAPTOP-001");
        laptop.setName("Laptop");
        laptop.setPrice(new BigDecimal("1000.00"));
        laptop.setCurrency("USD");
        laptop.setTotalStock(10);
        laptop.setAvailableStock(10);
        laptop.setReservedStock(0);
        laptop.setActive(true);
        laptop.setVisible(true);
        laptop.setDiscontinued(false);
        laptop.setAvailableForPurchase(true);
        entityManager.persist(laptop);
        
        ProductJpaEntity mouse = new ProductJpaEntity();
        mouse.setId("PROD-002");
        mouse.setSku("MOUSE-001");
        mouse.setName("Mouse");
        mouse.setPrice(new BigDecimal("25.00"));
        mouse.setCurrency("USD");
        mouse.setTotalStock(50);
        mouse.setAvailableStock(50);
        mouse.setReservedStock(0);
        mouse.setActive(true);
        mouse.setVisible(true);
        mouse.setDiscontinued(false);
        mouse.setAvailableForPurchase(true);
        entityManager.persist(mouse);
        
        String laptopId = laptop.getId();
        String mouseId = mouse.getId();
        
        entityManager.flush();
        entityManager.clear();
        
        System.out.println("Products created and flushed");
        
        // Create order
        LocalDateTime orderTimestamp = startDate.plusDays(5).atTime(10, 0);
        OrderJpaEntity order = new OrderJpaEntity();
        order.setId("ORD-001");
        order.setStatus(OrderStatus.DELIVERED);
        order.setCreatedAt(orderTimestamp);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCustomerId("1");
        order.setItems(new ArrayList<>());
        
        OrderItemJpaEntity item1 = new OrderItemJpaEntity();
        item1.setProductId(laptopId);
        item1.setProductName("Laptop");
        item1.setQuantity(5);
        item1.setPrice(new BigDecimal("1000.00"));
        order.getItems().add(item1);
        
        OrderItemJpaEntity item2 = new OrderItemJpaEntity();
        item2.setProductId(mouseId);
        item2.setProductName("Mouse");
        item2.setQuantity(20);
        item2.setPrice(new BigDecimal("25.00"));
        order.getItems().add(item2);
        
        entityManager.persist(order);
        entityManager.flush();
        
        // CRITICAL: Overwrite JPA Auditing timestamp using native SQL (bypasses @CreatedDate)
        entityManager.createNativeQuery(
            "UPDATE orders SET created_at = :timestamp WHERE id = :id"
        ).setParameter("timestamp", orderTimestamp)
         .setParameter("id", order.getId())
         .executeUpdate();
        
        entityManager.clear();
        
        System.out.println("Order created and flushed");
        
        // Query
        var result = analyticsRepository.getBestSellingProducts(10, startDate, endDate);
        System.out.println("Repository returned " + result.size() + " results");
        
        assertThat(result).as("Should return products").isNotEmpty();
        assertThat(result.get(0).getProductName()).isEqualTo("Mouse");
    }
}
