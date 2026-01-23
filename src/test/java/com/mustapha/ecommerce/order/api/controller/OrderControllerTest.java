package com.mustapha.ecommerce.order.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.application.facade.OrderFacade;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;
import com.mustapha.ecommerce.order.infrastructure.exception.OrderNotFoundException;
import com.mustapha.ecommerce.order.api.OrderController;
import com.mustapha.ecommerce.order.api.OrderGlobalExceptionHandler;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * REST API tests for OrderController.
 * Tests HTTP layer, request/response serialization, status codes, and error handling.
 */
@WebMvcTest({OrderController.class, OrderGlobalExceptionHandler.class})
@DisplayName("OrderController REST API Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderFacade orderFacade;

    private OrderResponse mockOrderResponse;

    @BeforeEach
    void setUp() {
        mockOrderResponse = new OrderResponse();
        mockOrderResponse.setOrderId("ORD-123");
        mockOrderResponse.setCustomerId("CUST-001");
        mockOrderResponse.setStatus("CONFIRMED");
        mockOrderResponse.setTotalAmount(299.99);
        mockOrderResponse.setCreatedAt(LocalDateTime.now());
        mockOrderResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/orders - Create Order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order and return 201 Created with order details")
        void shouldCreateOrderSuccessfully() throws Exception {
            // Arrange
            OrderRequest request = createValidOrderRequest();
            when(orderFacade.createOrder(any(OrderRequest.class))).thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.customerId").value("CUST-001"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(299.99))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

            verify(orderFacade, times(1)).createOrder(any(OrderRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when customerId is missing")
        void shouldReturn400WhenCustomerIdMissing() throws Exception {
            // Arrange
            OrderRequest request = new OrderRequest();
            request.setCustomerId(null); // Missing customerId
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-001", "Laptop", 1, 999.99)
            ));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when items list is empty")
        void shouldReturn400WhenItemsEmpty() throws Exception {
            // Arrange
            OrderRequest request = new OrderRequest();
            request.setCustomerId("CUST-001");
            request.setItems(Arrays.asList()); // Empty items

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is invalid JSON")
        void shouldReturn400WhenInvalidJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept request with multiple items")
        void shouldAcceptMultipleItems() throws Exception {
            // Arrange
            OrderRequest request = new OrderRequest();
            request.setCustomerId("CUST-001");
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-001", "Laptop", 1, 999.99),
                new OrderItemRequest("PROD-002", "Mouse", 2, 29.99),
                new OrderItemRequest("PROD-003", "Keyboard", 1, 79.99)
            ));

            mockOrderResponse.setTotalAmount(1139.96); // Updated total
            when(orderFacade.createOrder(any(OrderRequest.class))).thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(1139.96));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id} - Get Order")
    class GetOrderTests {

        @Test
        @DisplayName("Should return order details with 200 OK")
        void shouldGetOrderSuccessfully() throws Exception {
            // Arrange
            String orderId = "ORD-123";
            when(orderFacade.getOrder(orderId)).thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(get("/api/orders/{id}", orderId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.customerId").value("CUST-001"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(299.99));

            verify(orderFacade, times(1)).getOrder(orderId);
        }

        @Test
        @DisplayName("Should return 404 Not Found when order does not exist")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Arrange
            String orderId = "ORD-999";
            when(orderFacade.getOrder(orderId))
                .thenThrow(new OrderNotFoundException(new OrderId(orderId)));

            // Act & Assert
            mockMvc.perform(get("/api/orders/{id}", orderId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

            verify(orderFacade, times(1)).getOrder(orderId);
        }

        @Test
        @DisplayName("Should accept various orderId formats")
        void shouldAcceptVariousOrderIdFormats() throws Exception {
            // Arrange
            String orderId = "ORD-ABC-123-XYZ";
            when(orderFacade.getOrder(orderId)).thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(get("/api/orders/{id}", orderId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{id}/pay - Pay Order")
    class PayOrderTests {

        @Test
        @DisplayName("Should mark order as paid and return 200 OK")
        void shouldPayOrderSuccessfully() throws Exception {
            // Arrange
            String orderId = "ORD-123";
            mockOrderResponse.setStatus("PAID");
            when(orderFacade.payOrder(orderId, "credit_card", "tok_visa", 299.99))
                .thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/pay", orderId)
                    .param("paymentMethod", "credit_card")
                    .param("paymentToken", "tok_visa")
                    .param("amount", "299.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.orderId").value("ORD-123"));

            verify(orderFacade, times(1)).payOrder(orderId, "credit_card", "tok_visa", 299.99);
        }

        @Test
        @DisplayName("Should return 404 when paying non-existent order")
        void shouldReturn404WhenOrderNotFoundForPayment() throws Exception {
            // Arrange
            String orderId = "ORD-999";
            when(orderFacade.payOrder(eq(orderId), anyString(), anyString(), anyDouble()))
                .thenThrow(new OrderNotFoundException(new OrderId(orderId)));

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/pay", orderId)
                    .param("paymentMethod", "credit_card")
                    .param("paymentToken", "tok_visa")
                    .param("amount", "299.99"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when payment details are missing")
        void shouldReturn400WhenPaymentDetailsMissing() throws Exception {
            // Arrange
            String orderId = "ORD-123";

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/pay", orderId)
                    .param("paymentMethod", "credit_card"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{id}/ship - Ship Order")
    class ShipOrderTests {

        @Test
        @DisplayName("Should mark order as shipped and return 200 OK")
        void shouldShipOrderSuccessfully() throws Exception {
            // Arrange
            String orderId = "ORD-123";
            mockOrderResponse.setStatus("SHIPPED");
            when(orderFacade.shipOrder(orderId, "TRACK-123456", "FedEx"))
                .thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/ship", orderId)
                    .param("trackingNumber", "TRACK-123456")
                    .param("carrier", "FedEx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.orderId").value("ORD-123"));

            verify(orderFacade, times(1)).shipOrder(orderId, "TRACK-123456", "FedEx");
        }

        @Test
        @DisplayName("Should return 404 when shipping non-existent order")
        void shouldReturn404WhenOrderNotFoundForShipping() throws Exception {
            // Arrange
            String orderId = "ORD-999";
            when(orderFacade.shipOrder(eq(orderId), anyString(), anyString()))
                .thenThrow(new OrderNotFoundException(new OrderId(orderId)));

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/ship", orderId)
                    .param("trackingNumber", "TRACK-123456")
                    .param("carrier", "FedEx"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when tracking number is missing")
        void shouldReturn400WhenTrackingNumberMissing() throws Exception {
            // Arrange
            String orderId = "ORD-123";

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/ship", orderId)
                    .param("carrier", "FedEx"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{id}/deliver - Deliver Order")
    class DeliverOrderTests {

        @Test
        @DisplayName("Should mark order as delivered and return 200 OK")
        void shouldDeliverOrderSuccessfully() throws Exception {
            // Arrange
            String orderId = "ORD-123";
            mockOrderResponse.setStatus("DELIVERED");
            when(orderFacade.deliverOrder(eq(orderId), any(LocalDateTime.class)))
                .thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/deliver", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.orderId").value("ORD-123"));

            verify(orderFacade, times(1)).deliverOrder(eq(orderId), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should return 404 when delivering non-existent order")
        void shouldReturn404WhenOrderNotFoundForDelivery() throws Exception {
            // Arrange
            String orderId = "ORD-999";
            when(orderFacade.deliverOrder(eq(orderId), any(LocalDateTime.class)))
                .thenThrow(new OrderNotFoundException(new OrderId(orderId)));

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/deliver", orderId))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should use current timestamp for delivery")
        void shouldUseCurrentTimestampForDelivery() throws Exception {
            // Arrange
            String orderId = "ORD-123";
            mockOrderResponse.setStatus("DELIVERED");
            when(orderFacade.deliverOrder(eq(orderId), any(LocalDateTime.class)))
                .thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/deliver", orderId))
                .andExpect(status().isOk());

            // Verify that deliverOrder was called with a LocalDateTime (current time)
            verify(orderFacade, times(1)).deliverOrder(eq(orderId), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{id}/cancel - Cancel Order")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order and return 200 OK")
        void shouldCancelOrderSuccessfully() throws Exception {
            // Arrange
            String orderId = "ORD-123";
            mockOrderResponse.setStatus("CANCELLED");
            when(orderFacade.cancelOrder(orderId, "Customer changed mind"))
                .thenReturn(mockOrderResponse);

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                    .param("reason", "Customer changed mind"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.orderId").value("ORD-123"));

            verify(orderFacade, times(1)).cancelOrder(orderId, "Customer changed mind");
        }

        @Test
        @DisplayName("Should return 404 when cancelling non-existent order")
        void shouldReturn404WhenOrderNotFoundForCancellation() throws Exception {
            // Arrange
            String orderId = "ORD-999";
            when(orderFacade.cancelOrder(eq(orderId), anyString()))
                .thenThrow(new OrderNotFoundException(new OrderId(orderId)));

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                    .param("reason", "Customer changed mind"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when cancellation reason is missing")
        void shouldReturn400WhenReasonMissing() throws Exception {
            // Arrange
            String orderId = "ORD-123";

            // Act & Assert
            mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept various cancellation reasons")
        void shouldAcceptVariousCancellationReasons() throws Exception {
            // Arrange
            String orderId = "ORD-123";
            mockOrderResponse.setStatus("CANCELLED");
            when(orderFacade.cancelOrder(eq(orderId), anyString()))
                .thenReturn(mockOrderResponse);

            List<String> reasons = Arrays.asList(
                "Customer changed mind",
                "Out of stock",
                "Duplicate order",
                "Payment failed"
            );

            // Act & Assert
            for (String reason : reasons) {
                mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                        .param("reason", reason))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
            }

            verify(orderFacade, times(reasons.size())).cancelOrder(eq(orderId), anyString());
        }
    }

    // ========== Helper Methods ==========

    private OrderRequest createValidOrderRequest() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId("CUST-001");
        request.setItems(Arrays.asList(
            new OrderItemRequest("PROD-001", "Laptop", 1, 999.99),
            new OrderItemRequest("PROD-002", "Mouse", 1, 29.99)
        ));
        return request;
    }
}
