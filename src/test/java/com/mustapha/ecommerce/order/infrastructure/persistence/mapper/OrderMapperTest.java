package com.mustapha.ecommerce.order.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;

/**
 * Infrastructure Tests - OrderMapper
 * Tests bidirectional conversion between domain Order and JPA entities
 */
@DisplayName("OrderMapper Tests")
class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Nested
    @DisplayName("Domain to Entity Conversion")
    class ToEntityTests {

        @Test
        @DisplayName("Should convert Order domain to OrderJpaEntity")
        void shouldConvertOrderToEntity() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-001");
            OrderItem item1 = new OrderItem(new ProductId("PROD-001"), "Laptop", 2, new Money(999.99));
            OrderItem item2 = new OrderItem(new ProductId("PROD-002"), "Mouse", 1, new Money(29.99));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item1)
                .addItem(item2)
                .build();
            order.confirm();

            // Act
            OrderJpaEntity entity = orderMapper.toEntity(order);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(order.getId().getValue());
            assertThat(entity.getCustomerId()).isEqualTo("CUST-001");
            assertThat(entity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(entity.getTotalAmount()).isEqualByComparingTo("2029.97"); // (2 * 999.99) + 29.99
            assertThat(entity.getItems()).hasSize(2);
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should convert OrderItems to OrderItemJpaEntities")
        void shouldConvertOrderItems() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-001");
            OrderItem item = new OrderItem(new ProductId("PROD-123"), "TestProduct", 5, new Money(49.99));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();

            // Act
            OrderJpaEntity entity = orderMapper.toEntity(order);

            // Assert
            assertThat(entity.getItems()).hasSize(1);
            OrderItemJpaEntity itemEntity = entity.getItems().get(0);
            assertThat(itemEntity.getProductId()).isEqualTo("PROD-123");
            assertThat(itemEntity.getProductName()).isEqualTo("TestProduct");
            assertThat(itemEntity.getQuantity()).isEqualTo(5);
            assertThat(itemEntity.getPrice()).isEqualByComparingTo("49.99");
        }

        @Test
        @DisplayName("Should preserve Money precision as BigDecimal")
        void shouldPreserveMoneyPrecision() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-001");
            OrderItem item = new OrderItem(new ProductId("PROD-001"), "Product", 3, new Money(19.99));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();

            // Act
            OrderJpaEntity entity = orderMapper.toEntity(order);

            // Assert
            assertThat(entity.getTotalAmount()).isEqualByComparingTo("59.97"); // 3 * 19.99
            assertThat(entity.getItems().get(0).getPrice()).isEqualByComparingTo("19.99");
        }

        @Test
        @DisplayName("Should map all OrderStatus values correctly")
        void shouldMapAllOrderStatuses() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-001");
            OrderItem item = new OrderItem(new ProductId("PROD-001"), "Product", 1, new Money(100.0));

            // Test CONFIRMED
            Order confirmedOrder = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            confirmedOrder.confirm();

            // Test PAID
            Order paidOrder = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            paidOrder.confirm();
            paidOrder.markAsPaid();

            // Test CANCELLED
            Order cancelledOrder = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            cancelledOrder.confirm();
            cancelledOrder.cancel("Test cancellation");

            // Act & Assert
            assertThat(orderMapper.toEntity(confirmedOrder).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(orderMapper.toEntity(paidOrder).getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(orderMapper.toEntity(cancelledOrder).getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Entity to Domain Conversion")
    class ToDomainTests {

        @Test
        @DisplayName("Should convert OrderJpaEntity to Order domain using reconstitute()")
        void shouldConvertEntityToOrder() {
            // Arrange
            OrderJpaEntity entity = new OrderJpaEntity();
            entity.setId("ORDER-001");
            entity.setCustomerId("CUST-001");
            entity.setStatus(OrderStatus.CONFIRMED);
            entity.setTotalAmount(new java.math.BigDecimal("299.99"));
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            OrderItemJpaEntity item1 = new OrderItemJpaEntity();
            item1.setProductId("PROD-001");
            item1.setProductName("Laptop");
            item1.setQuantity(1);
            item1.setPrice(new java.math.BigDecimal("299.99"));

            entity.setItems(List.of(item1));

            // Act
            Order order = orderMapper.toDomain(entity);

            // Assert
            assertThat(order).isNotNull();
            assertThat(order.getId().getValue()).isEqualTo("ORDER-001");
            assertThat(order.getCustomerId().getValue()).isEqualTo("CUST-001");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(order.getTotalAmount().getAmount()).isEqualTo(299.99);
            assertThat(order.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("Should reconstruct OrderItems with correct value objects")
        void shouldReconstructOrderItems() {
            // Arrange
            OrderJpaEntity entity = new OrderJpaEntity();
            entity.setId("ORD-456");
            entity.setCustomerId("CUST-002");
            entity.setStatus(OrderStatus.PAID);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            OrderItemJpaEntity item1 = new OrderItemJpaEntity();
            item1.setProductId("PROD-001");
            item1.setProductName("Keyboard");
            item1.setQuantity(2);
            item1.setPrice(new java.math.BigDecimal("79.99"));

            OrderItemJpaEntity item2 = new OrderItemJpaEntity();
            item2.setProductId("PROD-002");
            item2.setProductName("Mouse");
            item2.setQuantity(1);
            item2.setPrice(new java.math.BigDecimal("49.99"));

            entity.setItems(List.of(item1, item2));
            entity.setTotalAmount(new java.math.BigDecimal("209.97")); // (2 * 79.99) + 49.99

            // Act
            Order order = orderMapper.toDomain(entity);

            // Assert
            assertThat(order.getItems()).hasSize(2);
            
            OrderItem firstItem = order.getItems().get(0);
            assertThat(firstItem.getProductId().getValue()).isEqualTo("PROD-001");
            assertThat(firstItem.getProductName()).isEqualTo("Keyboard");
            assertThat(firstItem.getQuantity()).isEqualTo(2);
            assertThat(firstItem.getPrice().getAmount()).isEqualTo(79.99);

            OrderItem secondItem = order.getItems().get(1);
            assertThat(secondItem.getProductId().getValue()).isEqualTo("PROD-002");
            assertThat(secondItem.getProductName()).isEqualTo("Mouse");
            assertThat(secondItem.getQuantity()).isEqualTo(1);
            assertThat(secondItem.getPrice().getAmount()).isEqualTo(49.99);
        }

        @Test
        @DisplayName("Should preserve BigDecimal precision when converting to Money")
        void shouldPreserveBigDecimalPrecision() {
            // Arrange
            OrderJpaEntity entity = new OrderJpaEntity();
            entity.setId("ORD-789");
            entity.setCustomerId("CUST-003");
            entity.setStatus(OrderStatus.DELIVERED);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            OrderItemJpaEntity item = new OrderItemJpaEntity();
            item.setProductId("PROD-001");
            item.setProductName("Product");
            item.setQuantity(1);
            item.setPrice(new java.math.BigDecimal("19.99"));

            entity.setItems(List.of(item));
            entity.setTotalAmount(new java.math.BigDecimal("19.99"));

            // Act
            Order order = orderMapper.toDomain(entity);

            // Assert
            assertThat(order.getTotalAmount().getAmount()).isEqualTo(19.99);
            assertThat(order.getItems().get(0).getPrice().getAmount()).isEqualTo(19.99);
        }

        @Test
        @DisplayName("Should use reconstitute() to bypass domain validation")
        void shouldUseReconstituteToBypassValidation() {
            // Arrange - Create entity with DELIVERED status (which can't be achieved through normal flow)
            OrderJpaEntity entity = new OrderJpaEntity();
            entity.setId("ORD-999");
            entity.setCustomerId("CUST-999");
            entity.setStatus(OrderStatus.DELIVERED);
            entity.setCreatedAt(LocalDateTime.now().minusDays(5));
            entity.setUpdatedAt(LocalDateTime.now());

            OrderItemJpaEntity item = new OrderItemJpaEntity();
            item.setProductId("PROD-999");
            item.setProductName("Historical Product");
            item.setQuantity(1);
            item.setPrice(new java.math.BigDecimal("999.99"));

            entity.setItems(List.of(item));
            entity.setTotalAmount(new java.math.BigDecimal("999.99"));

            // Act - Should not throw exception even though we're loading a DELIVERED order directly
            Order order = orderMapper.toDomain(entity);

            // Assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(order.getId().getValue()).isEqualTo("ORD-999");
            assertThat(order.getCreatedAt()).isBefore(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("Bidirectional Conversion")
    class BidirectionalTests {

        @Test
        @DisplayName("Should maintain data integrity in round-trip conversion")
        void shouldMaintainDataIntegrityInRoundTrip() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-ROUND");
            OrderItem item1 = new OrderItem(new ProductId("PROD-A"), "Product A", 3, new Money(33.33));
            OrderItem item2 = new OrderItem(new ProductId("PROD-B"), "Product B", 2, new Money(25.50));
            
            Order originalOrder = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item1)
                .addItem(item2)
                .build();
            originalOrder.confirm();

            // Act - Domain → Entity → Domain
            OrderJpaEntity entity = orderMapper.toEntity(originalOrder);
            Order reconstructedOrder = orderMapper.toDomain(entity);

            // Assert
            assertThat(reconstructedOrder.getId().getValue()).isEqualTo(originalOrder.getId().getValue());
            assertThat(reconstructedOrder.getCustomerId().getValue()).isEqualTo(originalOrder.getCustomerId().getValue());
            assertThat(reconstructedOrder.getStatus()).isEqualTo(originalOrder.getStatus());
            assertThat(reconstructedOrder.getTotalAmount().getAmount()).isEqualTo(originalOrder.getTotalAmount().getAmount());
            assertThat(reconstructedOrder.getItems()).hasSize(originalOrder.getItems().size());
            
            // Verify item details
            for (int i = 0; i < originalOrder.getItems().size(); i++) {
                OrderItem original = originalOrder.getItems().get(i);
                OrderItem reconstructed = reconstructedOrder.getItems().get(i);
                
                assertThat(reconstructed.getProductId().getValue()).isEqualTo(original.getProductId().getValue());
                assertThat(reconstructed.getProductName()).isEqualTo(original.getProductName());
                assertThat(reconstructed.getQuantity()).isEqualTo(original.getQuantity());
                assertThat(reconstructed.getPrice().getAmount()).isEqualTo(original.getPrice().getAmount());
            }
        }
    }
}
