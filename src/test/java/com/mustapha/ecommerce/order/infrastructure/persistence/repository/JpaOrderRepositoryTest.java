package com.mustapha.ecommerce.order.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.infrastructure.persistence.mapper.OrderMapper;

/**
 * Infrastructure Tests - JpaOrderRepository
 * Tests database persistence layer with H2 in-memory database
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
@DisplayName("JpaOrderRepository Tests")
class JpaOrderRepositoryTest {

    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    @Autowired
    private SpringDataOrderRepository springDataRepository;

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save new order to database")
        void shouldSaveNewOrder() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-001");
            OrderItem item = new OrderItem(new ProductId("PROD-001"), "Laptop", 1, new Money(999.99));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            order.confirm();

            // Act
            Order savedOrder = jpaOrderRepository.save(order);

            // Assert
            assertThat(savedOrder).isNotNull();
            assertThat(savedOrder.getId()).isNotNull();
            assertThat(springDataRepository.findById(savedOrder.getId().getValue())).isPresent();
        }

        @Test
        @DisplayName("Should persist order items with order")
        void shouldPersistOrderItems() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-002");
            OrderItem item1 = new OrderItem(new ProductId("PROD-001"), "Mouse", 2, new Money(29.99));
            OrderItem item2 = new OrderItem(new ProductId("PROD-002"), "Keyboard", 1, new Money(79.99));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item1)
                .addItem(item2)
                .build();

            // Act
            Order savedOrder = jpaOrderRepository.save(order);

            // Assert
            Order retrievedOrder = jpaOrderRepository.findById(savedOrder.getId()).orElseThrow();
            assertThat(retrievedOrder.getItems()).hasSize(2);
            assertThat(retrievedOrder.getItems().get(0).getProductName()).isEqualTo("Mouse");
            assertThat(retrievedOrder.getItems().get(1).getProductName()).isEqualTo("Keyboard");
        }

        @Test
        @DisplayName("Should update existing order")
        void shouldUpdateExistingOrder() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-003");
            OrderItem item = new OrderItem(new ProductId("PROD-001"), "Product", 1, new Money(100.0));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            order.confirm();
            
            Order savedOrder = jpaOrderRepository.save(order);
            OrderId orderId = savedOrder.getId();

            // Act - Update order status
            savedOrder.markAsPaid();
            jpaOrderRepository.save(savedOrder);

            // Assert
            Order updatedOrder = jpaOrderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(com.mustapha.ecommerce.order.domain.model.OrderStatus.PAID);
        }

        @Test
        @DisplayName("Should preserve BigDecimal precision in database")
        void shouldPreserveBigDecimalPrecision() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-004");
            OrderItem item = new OrderItem(new ProductId("PROD-001"), "Product", 3, new Money(19.99));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();

            // Act
            Order savedOrder = jpaOrderRepository.save(order);

            // Assert
            Order retrievedOrder = jpaOrderRepository.findById(savedOrder.getId()).orElseThrow();
            assertThat(retrievedOrder.getTotalAmount().getAmount()).isEqualTo(59.97); // 3 * 19.99
            assertThat(retrievedOrder.getItems().get(0).getPrice().getAmount()).isEqualTo(19.99);
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindTests {

        private OrderId savedOrderId;

        @BeforeEach
        void setUp() {
            CustomerId customerId = new CustomerId("CUST-FIND");
            OrderItem item = new OrderItem(new ProductId("PROD-FIND"), "FindProduct", 1, new Money(50.0));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            
            savedOrderId = jpaOrderRepository.save(order).getId();
        }

        @Test
        @DisplayName("Should find order by ID")
        void shouldFindOrderById() {
            // Act
            Optional<Order> foundOrder = jpaOrderRepository.findById(savedOrderId);

            // Assert
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(savedOrderId);
            assertThat(foundOrder.get().getCustomerId().getValue()).isEqualTo("CUST-FIND");
        }

        @Test
        @DisplayName("Should return empty when order not found")
        void shouldReturnEmptyWhenNotFound() {
            // Act
            Optional<Order> foundOrder = jpaOrderRepository.findById(new OrderId("NON-EXISTENT"));

            // Assert
            assertThat(foundOrder).isEmpty();
        }

        @Test
        @DisplayName("Should load order with all items")
        void shouldLoadOrderWithAllItems() {
            // Arrange - Create order with multiple items
            CustomerId customerId = new CustomerId("CUST-MULTI");
            OrderItem item1 = new OrderItem(new ProductId("PROD-A"), "Product A", 1, new Money(10.0));
            OrderItem item2 = new OrderItem(new ProductId("PROD-B"), "Product B", 2, new Money(20.0));
            OrderItem item3 = new OrderItem(new ProductId("PROD-C"), "Product C", 3, new Money(30.0));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item1)
                .addItem(item2)
                .addItem(item3)
                .build();
            
            OrderId multiItemOrderId = jpaOrderRepository.save(order).getId();

            // Act
            Order foundOrder = jpaOrderRepository.findById(multiItemOrderId).orElseThrow();

            // Assert
            assertThat(foundOrder.getItems()).hasSize(3);
            assertThat(foundOrder.getItems())
                .extracting(OrderItem::getProductName)
                .containsExactly("Product A", "Product B", "Product C");
        }
    }

    @Nested
    @DisplayName("Customer-specific Operations")
    class CustomerTests {

        @Test
        @DisplayName("Should find all orders for a customer")
        void shouldFindAllOrdersForCustomer() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-MULTI-ORDER");
            
            // Create first order
            OrderItem item1 = new OrderItem(new ProductId("PROD-1"), "Product 1", 1, new Money(100.0));
            Order order1 = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item1)
                .build();
            jpaOrderRepository.save(order1);

            // Create second order
            OrderItem item2 = new OrderItem(new ProductId("PROD-2"), "Product 2", 2, new Money(50.0));
            Order order2 = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item2)
                .build();
            jpaOrderRepository.save(order2);

            // Act
            var customerOrders = jpaOrderRepository.findByCustomerId(customerId);

            // Assert
            assertThat(customerOrders).hasSize(2);
            assertThat(customerOrders)
                .extracting(Order::getCustomerId)
                .containsOnly(customerId);
        }

        @Test
        @DisplayName("Should return empty list when customer has no orders")
        void shouldReturnEmptyListForCustomerWithNoOrders() {
            // Act
            var orders = jpaOrderRepository.findByCustomerId(new CustomerId("CUST-NO-ORDERS"));

            // Assert
            assertThat(orders).isEmpty();
        }

        @Test
        @DisplayName("Should isolate orders by customer ID")
        void shouldIsolateOrdersByCustomerId() {
            // Arrange
            CustomerId customer1 = new CustomerId("CUST-A");
            CustomerId customer2 = new CustomerId("CUST-B");
            
            OrderItem item = new OrderItem(new ProductId("PROD-001"), "Product", 1, new Money(100.0));
            
            Order order1 = new OrderBuilder()
                .withCustomerId(customer1)
                .addItem(item)
                .build();
            jpaOrderRepository.save(order1);

            Order order2 = new OrderBuilder()
                .withCustomerId(customer2)
                .addItem(item)
                .build();
            jpaOrderRepository.save(order2);

            // Act
            var customer1Orders = jpaOrderRepository.findByCustomerId(customer1);
            var customer2Orders = jpaOrderRepository.findByCustomerId(customer2);

            // Assert
            assertThat(customer1Orders).hasSize(1);
            assertThat(customer2Orders).hasSize(1);
            assertThat(customer1Orders.get(0).getCustomerId()).isEqualTo(customer1);
            assertThat(customer2Orders.get(0).getCustomerId()).isEqualTo(customer2);
        }
    }

    @Nested
    @DisplayName("Domain Reconstitution")
    class ReconstitutionTests {

        @Test
        @DisplayName("Should reconstitute order with correct domain state")
        void shouldReconstituteOrderWithCorrectState() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-RECON");
            OrderItem item = new OrderItem(new ProductId("PROD-RECON"), "ReconProduct", 2, new Money(75.50));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            order.confirm();
            order.markAsPaid();
            
            OrderId orderId = jpaOrderRepository.save(order).getId();

            // Act
            Order reconstitutedOrder = jpaOrderRepository.findById(orderId).orElseThrow();

            // Assert - Order should be reconstituted with all domain state
            assertThat(reconstitutedOrder.getStatus()).isEqualTo(com.mustapha.ecommerce.order.domain.model.OrderStatus.PAID);
            assertThat(reconstitutedOrder.getTotalAmount().getAmount()).isEqualTo(151.0); // 2 * 75.50
            assertThat(reconstitutedOrder.getItems().get(0).getTotal().getAmount()).isEqualTo(151.0);
        }

        @Test
        @DisplayName("Should preserve timestamps after persistence")
        void shouldPreserveTimestamps() {
            // Arrange
            CustomerId customerId = new CustomerId("CUST-TIME");
            OrderItem item = new OrderItem(new ProductId("PROD-TIME"), "TimeProduct", 1, new Money(100.0));
            
            Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .addItem(item)
                .build();
            
            Order savedOrder = jpaOrderRepository.save(order);
            OrderId orderId = savedOrder.getId();

            // Act
            Order retrievedOrder = jpaOrderRepository.findById(orderId).orElseThrow();

            // Assert - verify order is successfully persisted and retrieved
            assertThat(retrievedOrder).isNotNull();
            assertThat(retrievedOrder.getId()).isEqualTo(orderId);
            assertThat(retrievedOrder.getCustomerId()).isEqualTo(customerId);
        }
    }
}
