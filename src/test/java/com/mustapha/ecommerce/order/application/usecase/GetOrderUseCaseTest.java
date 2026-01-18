package com.mustapha.ecommerce.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mustapha.ecommerce.order.application.command.GetOrderQuery;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

@DisplayName("GetOrderUseCase")
class GetOrderUseCaseTest {

    private OrderRepository orderRepository;
    private GetOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        useCase = new GetOrderUseCase(orderRepository);
    }

    @Test
    @DisplayName("Should retrieve order by ID")
    void shouldRetrieveOrderById() {
        // Given
        Order order = new OrderBuilder()
            .withCustomerId(new CustomerId("customer-123"))
            .addItem(new OrderItem(new ProductId("p1"), "Product", 1, new Money(50.0)))
            .build();

        GetOrderQuery query = new GetOrderQuery(order.getId());

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // When
        Order result = useCase.execute(query);

        // Then
        assertThat(result).isEqualTo(order);
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getCustomerId()).isEqualTo(new CustomerId("customer-123"));
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        OrderId nonExistent = new OrderId("non-existent");
        GetOrderQuery query = new GetOrderQuery(nonExistent);

        when(orderRepository.findById(nonExistent)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.execute(query))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order not found");
    }
}
