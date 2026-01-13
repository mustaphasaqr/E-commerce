package com.mustapha.ecommerce.order.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.event.OrderPlacedEvent;
import com.mustapha.ecommerce.order.domain.exception.InvalidOrderItemException;
import com.mustapha.ecommerce.order.domain.exception.InvalidOrderStateException;
import com.mustapha.ecommerce.order.domain.exception.OrderModificationNotAllowedException;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain Tests for Order Aggregate
 * 
 * Focus: Business rules, invariants, state transitions, domain events
 * NOT testing: Spring, DB, HTTP - pure domain logic only
 * 
 * Reviewer's requirement: "لو الجزء ده ملوش Tests: المعمارية هتنهار بعد شهر"
 */
@DisplayName("Order Aggregate Domain Tests")
class OrderTest {

    private OrderBuilder orderBuilder;
    private CustomerId customerId;
    private ProductId productId1;
    private ProductId productId2;

    @BeforeEach
    void setUp() {
        customerId = CustomerId.generate();
        productId1 = ProductId.generate();
        productId2 = ProductId.generate();
        
        orderBuilder = new OrderBuilder()
            .withCustomerId(customerId.getValue());
    }

    // ========== Order Creation Tests ==========
    
    @Nested
    @DisplayName("Order Creation")
    class OrderCreationTests {
        
        @Test
        @DisplayName("Should create order with items successfully")
        void shouldCreateOrderWithItems() {
            // Given
            List<java.util.Map<String, Object>> items = List.of(
                java.util.Map.of(
                    "productId", productId1.getValue(),
                    "productName", "Product 1",
                    "quantity", 2,
                    "price", 50.0
                )
            );
            
            // When
            Order order = orderBuilder.withItems(items).build();
            
            // Then
            assertNotNull(order);
            assertNotNull(order.getId());
            assertEquals(customerId, order.getCustomerId());
            assertEquals(1, order.getItems().size());
            assertEquals(new Money(100), order.getTotalAmount()); // 2 * 50 = 100
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertNotNull(order.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should fail when customer ID is missing")
        void shouldFailWhenCustomerIdMissing() {
            // Given
            OrderBuilder builderWithoutCustomer = new OrderBuilder();
            
            // When/Then
            assertThrows(IllegalStateException.class, () -> {
                builderWithoutCustomer.withItems(List.of(
                    java.util.Map.of(
                        "productId", productId1.getValue(),
                        "productName", "Product 1",
                        "quantity", 1,
                        "price", 100.0
                    )
                )).build();
            }, "Customer ID is required");
        }
        
        @Test
        @DisplayName("Should fail when order has no items")
        void shouldFailWhenOrderHasNoItems() {
            // When/Then
            assertThrows(IllegalStateException.class, () -> {
                orderBuilder.build();
            }, "Order must have at least one item");
        }
        
        @Test
        @DisplayName("Should calculate total correctly for multiple items")
        void shouldCalculateTotalCorrectly() {
            // Given
            List<java.util.Map<String, Object>> items = List.of(
                java.util.Map.of(
                    "productId", productId1.getValue(),
                    "productName", "Product 1",
                    "quantity", 2,
                    "price", 50.0
                ),
                java.util.Map.of(
                    "productId", productId2.getValue(),
                    "productName", "Product 2",
                    "quantity", 3,
                    "price", 30.0
                )
            );
            
            // When
            Order order = orderBuilder.withItems(items).build();
            
            // Then
            // (2 * 50) + (3 * 30) = 100 + 90 = 190
            assertEquals(new Money(190), order.getTotalAmount());
        }
    }

    // ========== State Transition Tests ==========
    
    @Nested
    @DisplayName("State Transitions - Happy Path")
    class StateTransitionHappyPathTests {
        
        private Order order;
        
        @BeforeEach
        void setUp() {
            order = createOrderWithOneItem();
        }
        
        @Test
        @DisplayName("PENDING → CONFIRMED should work and raise OrderPlacedEvent")
        void shouldTransitionFromPendingToConfirmed() {
            // When
            order.confirm();
            
            // Then
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
            assertNotNull(order.getUpdatedAt());
            
            // Verify event was raised
            List<DomainEvent> events = order.getDomainEvents();
            assertEquals(1, events.size());
            assertTrue(events.get(0) instanceof OrderPlacedEvent);
            
            OrderPlacedEvent event = (OrderPlacedEvent) events.get(0);
            assertEquals(order.getId(), event.orderId());
            assertEquals(customerId, event.customerId());
            assertEquals(order.getTotalAmount(), event.totalAmount());
        }
        
        @Test
        @DisplayName("CONFIRMED → PAID should work")
        void shouldTransitionFromConfirmedToPaid() {
            // Given
            order.confirm();
            order.clearDomainEvents(); // Clear OrderPlacedEvent
            
            // When
            order.markAsPaid();
            
            // Then
            assertEquals(OrderStatus.PAID, order.getStatus());
            assertTrue(order.isPaid());
        }
        
        @Test
        @DisplayName("PAID → PROCESSING should work")
        void shouldTransitionFromPaidToProcessing() {
            // Given
            order.confirm();
            order.markAsPaid();
            
            // When
            order.startProcessing();
            
            // Then
            assertEquals(OrderStatus.PROCESSING, order.getStatus());
        }
        
        @Test
        @DisplayName("PROCESSING → SHIPPED should work")
        void shouldTransitionFromProcessingToShipped() {
            // Given
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            
            // When
            order.ship();
            
            // Then
            assertEquals(OrderStatus.SHIPPED, order.getStatus());
        }
        
        @Test
        @DisplayName("SHIPPED → DELIVERED should work")
        void shouldTransitionFromShippedToDelivered() {
            // Given
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            order.ship();
            
            // When
            order.deliver();
            
            // Then
            assertEquals(OrderStatus.DELIVERED, order.getStatus());
        }
        
        @Test
        @DisplayName("Full lifecycle: PENDING → CONFIRMED → PAID → PROCESSING → SHIPPED → DELIVERED")
        void shouldCompleteFullLifecycle() {
            // When
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            order.ship();
            order.deliver();
            
            // Then
            assertEquals(OrderStatus.DELIVERED, order.getStatus());
            assertTrue(order.isPaid());
        }
    }
    
    @Nested
    @DisplayName("State Transitions - Invalid Transitions")
    class StateTransitionInvalidTests {
        
        private Order order;
        
        @BeforeEach
        void setUp() {
            order = createOrderWithOneItem();
        }
        
        @Test
        @DisplayName("Cannot confirm order with no items")
        void cannotConfirmOrderWithNoItems() {
            // Given - create order and remove items
            order.getItems().clear(); // This shouldn't work due to defensive copy, but test the validation
            
            // Actually need to test with empty order
            Order emptyOrder = new Order();
            emptyOrder.setCustomerId(customerId);
            
            // When/Then
            assertThrows(InvalidOrderStateException.class, () -> {
                emptyOrder.confirm();
            }, "Cannot confirm order with no items");
        }
        
        @Test
        @DisplayName("Cannot pay twice")
        void cannotPayTwice() {
            // Given
            order.confirm();
            order.markAsPaid();
            
            // When/Then
            assertThrows(InvalidOrderStateException.class, () -> {
                order.markAsPaid();
            }, "Cannot pay twice");
        }
        
        @Test
        @DisplayName("Cannot start processing if not paid")
        void cannotStartProcessingIfNotPaid() {
            // Given
            order.confirm();
            // NOT paid yet
            
            // When/Then
            assertThrows(InvalidOrderStateException.class, () -> {
                order.startProcessing();
            });
        }
        
        @Test
        @DisplayName("Cannot ship if not processing")
        void cannotShipIfNotProcessing() {
            // Given
            order.confirm();
            order.markAsPaid();
            // NOT processing yet
            
            // When/Then
            assertThrows(InvalidOrderStateException.class, () -> {
                order.ship();
            });
        }
        
        @Test
        @DisplayName("Cannot deliver if not shipped")
        void cannotDeliverIfNotShipped() {
            // Given
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            // NOT shipped yet
            
            // When/Then
            assertThrows(InvalidOrderStateException.class, () -> {
                order.deliver();
            });
        }
    }

    // ========== Cancellation Tests ==========
    
    @Nested
    @DisplayName("Order Cancellation")
    class OrderCancellationTests {
        
        @Test
        @DisplayName("Can cancel PENDING order")
        void canCancelPendingOrder() {
            // Given
            Order order = createOrderWithOneItem();
            
            // When
            order.cancel();
            
            // Then
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }
        
        @Test
        @DisplayName("Can cancel CONFIRMED order")
        void canCancelConfirmedOrder() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            
            // When
            order.cancel();
            
            // Then
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }
        
        @Test
        @DisplayName("Can cancel PAID order")
        void canCancelPaidOrder() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            order.markAsPaid();
            
            // When
            order.cancel();
            
            // Then
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
        }
        
        @Test
        @DisplayName("Cannot cancel SHIPPED order")
        void cannotCancelShippedOrder() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            order.ship();
            
            // When/Then
            assertThrows(InvalidOrderStateException.class, () -> {
                order.cancel();
            }, "Cannot cancel shipped order");
        }
        
        @Test
        @DisplayName("Cannot cancel DELIVERED order")
        void cannotCancelDeliveredOrder() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            order.ship();
            order.deliver();
            
            // When/Then
            assertThrows(InvalidOrderStateException.class, () -> {
                order.cancel();
            }, "Cannot cancel delivered order");
        }
    }

    // ========== Order Modification Tests ==========
    
    @Nested
    @DisplayName("Order Modification Rules")
    class OrderModificationTests {
        
        @Test
        @DisplayName("Can add items to PENDING order")
        void canAddItemsToPendingOrder() {
            // Given
            Order order = createOrderWithOneItem();
            OrderItem newItem = new OrderItem(productId2, "Product 2", 1, new Money(50));
            
            // When
            order.addItem(newItem);
            
            // Then
            assertEquals(2, order.getItems().size());
            assertEquals(new Money(150), order.getTotalAmount()); // 100 + 50
        }
        
        @Test
        @DisplayName("Cannot add items to CONFIRMED order")
        void cannotAddItemsToConfirmedOrder() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            OrderItem newItem = new OrderItem(productId2, "Product 2", 1, new Money(50));
            
            // When/Then
            assertThrows(OrderModificationNotAllowedException.class, () -> {
                order.addItem(newItem);
            }, "Cannot add items - order is not modifiable");
        }
        
        @Test
        @DisplayName("Cannot add null item")
        void cannotAddNullItem() {
            // Given
            Order order = createOrderWithOneItem();
            
            // When/Then
            assertThrows(InvalidOrderItemException.class, () -> {
                order.addItem(null);
            }, "Order item cannot be null");
        }
        
        @Test
        @DisplayName("Cannot exceed max total quantity")
        void cannotExceedMaxTotalQuantity() {
            // Given
            Order order = createOrderWithOneItem();
            // Create item with quantity that exceeds max (100)
            OrderItem hugeItem = new OrderItem(productId2, "Huge order", 100, new Money(1));
            
            // When/Then
            assertThrows(InvalidOrderItemException.class, () -> {
                order.addItem(hugeItem);
            }, "Cannot add item - would exceed max order quantity");
        }
        
        @Test
        @DisplayName("Can remove items from PENDING order")
        void canRemoveItemsFromPendingOrder() {
            // Given
            Order order = createOrderWithTwoItems();
            OrderItem itemToRemove = order.getItems().get(0);
            
            // When
            order.removeItem(itemToRemove);
            
            // Then
            assertEquals(1, order.getItems().size());
        }
        
        @Test
        @DisplayName("Cannot remove items from CONFIRMED order")
        void cannotRemoveItemsFromConfirmedOrder() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            OrderItem item = order.getItems().get(0);
            
            // When/Then
            assertThrows(OrderModificationNotAllowedException.class, () -> {
                order.removeItem(item);
            }, "Cannot remove items - order is not modifiable");
        }
    }

    // ========== Domain Events Tests ==========
    
    @Nested
    @DisplayName("Domain Events")
    class DomainEventsTests {
        
        @Test
        @DisplayName("OrderPlacedEvent should be raised on confirm()")
        void orderPlacedEventShouldBeRaisedOnConfirm() {
            // Given
            Order order = createOrderWithOneItem();
            
            // When
            order.confirm();
            
            // Then
            List<DomainEvent> events = order.getDomainEvents();
            assertEquals(1, events.size());
            assertTrue(events.get(0) instanceof OrderPlacedEvent);
            
            OrderPlacedEvent event = (OrderPlacedEvent) events.get(0);
            assertNotNull(event.eventId());
            assertEquals(order.getId(), event.orderId());
            assertEquals(order.getCustomerId(), event.customerId());
            assertEquals(order.getTotalAmount(), event.totalAmount());
            assertNotNull(event.occurredAt());
        }
        
        @Test
        @DisplayName("Can clear domain events after processing")
        void canClearDomainEventsAfterProcessing() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            assertEquals(1, order.getDomainEvents().size());
            
            // When
            order.clearDomainEvents();
            
            // Then
            assertEquals(0, order.getDomainEvents().size());
        }
        
        @Test
        @DisplayName("getDomainEvents returns defensive copy")
        void getDomainEventsReturnsDefensiveCopy() {
            // Given
            Order order = createOrderWithOneItem();
            order.confirm();
            
            // When
            List<DomainEvent> events = order.getDomainEvents();
            events.clear(); // Try to clear the copy
            
            // Then - original events should still exist
            assertEquals(1, order.getDomainEvents().size());
        }
    }

    // ========== Invariant Tests ==========
    
    @Nested
    @DisplayName("Invariants and Business Rules")
    class InvariantsTests {
        
        @Test
        @DisplayName("Total amount always equals sum of items")
        void totalAmountAlwaysEqualsSumOfItems() {
            // Given
            Order order = createOrderWithTwoItems();
            
            // When
            Money expectedTotal = order.getItems().stream()
                .map(OrderItem::getTotal)
                .reduce(new Money(0), Money::add);
            
            // Then
            assertEquals(expectedTotal, order.getTotalAmount());
        }
        
        @Test
        @DisplayName("getItems returns defensive copy")
        void getItemsReturnsDefensiveCopy() {
            // Given
            Order order = createOrderWithOneItem();
            int originalSize = order.getItems().size();
            
            // When
            List<OrderItem> items = order.getItems();
            items.clear(); // Try to modify the returned list
            
            // Then - original order should be unchanged
            assertEquals(originalSize, order.getItems().size());
        }
        
        @Test
        @DisplayName("isPaid returns true only when status is PAID or later")
        void isPaidReturnsTrueOnlyWhenPaid() {
            // Given
            Order order = createOrderWithOneItem();
            
            // Then
            assertFalse(order.isPaid()); // PENDING
            
            order.confirm();
            assertFalse(order.isPaid()); // CONFIRMED
            
            order.markAsPaid();
            assertTrue(order.isPaid()); // PAID
            
            order.startProcessing();
            assertTrue(order.isPaid()); // PROCESSING (still paid)
            
            order.ship();
            assertTrue(order.isPaid()); // SHIPPED (still paid)
            
            order.deliver();
            assertTrue(order.isPaid()); // DELIVERED (still paid)
        }
    }

    // ========== Helper Methods ==========
    
    private Order createOrderWithOneItem() {
        return orderBuilder.withItems(List.of(
            java.util.Map.of(
                "productId", productId1.getValue(),
                "productName", "Test Product",
                "quantity", 1,
                "price", 100.0
            )
        )).build();
    }
    
    private Order createOrderWithTwoItems() {
        return orderBuilder.withItems(List.of(
            java.util.Map.of(
                "productId", productId1.getValue(),
                "productName", "Product 1",
                "quantity", 2,
                "price", 50.0
            ),
            java.util.Map.of(
                "productId", productId2.getValue(),
                "productName", "Product 2",
                "quantity", 1,
                "price", 30.0
            )
        )).build();
    }
}
