package com.mustapha.ecommerce.order.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mustapha.ecommerce.order.application.facade.OrderFacade;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;

/**
 * HTTP Boundary - Order Controller
 * Responsibility: Request/Response mapping, Syntactic validation, HTTP error translation
 * Pattern: Facade (Controller → Application Facade)
 * SOLID: SRP (HTTP only), DIP (depends on application interfaces)
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderFacade.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        OrderResponse response = orderFacade.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> payOrder(
            @PathVariable String orderId,
            @RequestParam String paymentMethod,
            @RequestParam String paymentToken,
            @RequestParam double amount) {
        OrderResponse response = orderFacade.payOrder(orderId, paymentMethod, paymentToken, amount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponse> shipOrder(
            @PathVariable String orderId,
            @RequestParam String trackingNumber,
            @RequestParam String carrier) {
        OrderResponse response = orderFacade.shipOrder(orderId, trackingNumber, carrier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable String orderId) {
        OrderResponse response = orderFacade.deliverOrder(orderId, java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String reason) {
        OrderResponse response = orderFacade.cancelOrder(orderId, reason);
        return ResponseEntity.ok(response);
    }
}
