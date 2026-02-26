package com.mustapha.ecommerce.shared.security.authorization;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.shared.exception.ErrorCode;
import com.mustapha.ecommerce.shared.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceOwnershipService Tests")
class ResourceOwnershipServiceTest {

    private ResourceOwnershipService service;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Order order;

    private static final String USER_ID = "user-123";
    private static final String ORDER_ID = "order-456";
    private static final String OTHER_USER_ID = "user-999";

    @BeforeEach
    void setUp() {
        service = new ResourceOwnershipService(orderRepository);
    }

    @Test
    @DisplayName("Should verify ownership when user owns the order")
    void verifyOwnership_WhenUserOwnsOrder() {
        OrderId orderId = new OrderId(ORDER_ID);
        CustomerId customerId = new CustomerId(USER_ID);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getCustomerId()).thenReturn(customerId);

        service.checkOwnership(USER_ID, ORDER_ID, ResourceType.ORDER);

        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when user does not own order")
    void throwForbidden_WhenUserNotOwner() {
        OrderId orderId = new OrderId(ORDER_ID);
        CustomerId customerId = new CustomerId(OTHER_USER_ID);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getCustomerId()).thenReturn(customerId);

        assertThatThrownBy(() -> 
            service.checkOwnership(USER_ID, ORDER_ID, ResourceType.ORDER)
        )
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("You do not have permission")
        .extracting("errorCode")
        .isEqualTo(ErrorCode.AUTHZ_NOT_RESOURCE_OWNER);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when order not found")
    void throwForbidden_WhenOrderNotFound() {
        OrderId orderId = new OrderId(ORDER_ID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            service.checkOwnership(USER_ID, ORDER_ID, ResourceType.ORDER)
        )
        .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Should return true when user owns the order")
    void isOwner_ReturnsTrueForOwner() {
        OrderId orderId = new OrderId(ORDER_ID);
        CustomerId customerId = new CustomerId(USER_ID);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getCustomerId()).thenReturn(customerId);

        boolean result = service.isOwner(USER_ID, ORDER_ID, ResourceType.ORDER);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when user does not own order")
    void isOwner_ReturnsFalseForNonOwner() {
        OrderId orderId = new OrderId(ORDER_ID);
        CustomerId customerId = new CustomerId(OTHER_USER_ID);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getCustomerId()).thenReturn(customerId);

        boolean result = service.isOwner(USER_ID, ORDER_ID, ResourceType.ORDER);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when order not found")
    void isOwner_ReturnsFalseWhenOrderNotFound() {
        OrderId orderId = new OrderId(ORDER_ID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        boolean result = service.isOwner(USER_ID, ORDER_ID, ResourceType.ORDER);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle invalid order ID format gracefully")
    void handleInvalidOrderId() {
        String invalidOrderId = "not-a-valid-id";

        assertThatThrownBy(() -> 
            service.checkOwnership(USER_ID, invalidOrderId, ResourceType.ORDER)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Should return false for unimplemented resource types")
    void returnFalseForUnimplementedTypes() {
        boolean productOwnership = service.isOwner(USER_ID, "product-123", ResourceType.PRODUCT);
        boolean addressOwnership = service.isOwner(USER_ID, "address-123", ResourceType.ADDRESS);
        boolean cartOwnership = service.isOwner(USER_ID, "cart-123", ResourceType.CART);

        assertThat(productOwnership).isFalse();
        assertThat(addressOwnership).isFalse();
        assertThat(cartOwnership).isFalse();
    }

    @Test
    @DisplayName("Should check ownership for multiple resource types")
    void checkOwnershipForMultipleTypes() {
        assertThatThrownBy(() -> 
            service.checkOwnership(USER_ID, "product-123", ResourceType.PRODUCT)
        )
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("product");
    }

    @Test
    @DisplayName("Should verify ownership with custom error messages")
    void verifyOwnershipWithCustomMessages() {
        OrderId orderId = new OrderId(ORDER_ID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            service.checkOwnership(USER_ID, ORDER_ID, ResourceType.ORDER)
        )
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("order");
    }
}
