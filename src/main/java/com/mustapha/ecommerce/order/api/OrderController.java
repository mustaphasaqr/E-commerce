package com.mustapha.ecommerce.order.api;

import com.mustapha.ecommerce.order.application.port.TaxCalculationPort;
import com.mustapha.ecommerce.order.application.port.TaxCalculationPort.TaxCalculation;
import com.mustapha.ecommerce.order.application.port.TaxCalculationPort.TaxCalculationRequest;
import com.mustapha.ecommerce.shared.security.authorization.ResourceType;
import com.mustapha.ecommerce.shared.security.authorization.VerifyOwnership;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mustapha.ecommerce.order.application.facade.OrderFacade;
import com.mustapha.ecommerce.order.dto.OrderListResponse;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP Boundary - Order Controller
 * Responsibility: Request/Response mapping, Syntactic validation, HTTP error translation
 * Pattern: Facade (Controller → Application Facade)
 * SOLID: SRP (HTTP only), DIP (depends on application interfaces)
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "Order lifecycle management including creation, payment processing, shipping, delivery, and cancellation. Supports idempotent operations and tax calculation.")
public class OrderController {

    private final OrderFacade orderFacade;
    private final TaxCalculationPort taxCalculationPort;
    private final ConcurrentHashMap<String, OrderResponse> idempotencyCache = new ConcurrentHashMap<>();

    public OrderController(OrderFacade orderFacade, TaxCalculationPort taxCalculationPort) {
        this.orderFacade = orderFacade;
        this.taxCalculationPort = taxCalculationPort;
    }

    @Operation(
        summary = "Create Order",
        description = """
            Create a new order from cart items. Supports idempotent operations via Idempotency-Key header.
            
            **Features:**
            - Creates order from cart
            - Reserves product stock
            - Calculates totals and taxes
            - Idempotent with Idempotency-Key header
            - Validates payment information
            
            **Idempotency:** Provide Idempotency-Key header to prevent duplicate orders
            
            **Security:** Requires authentication
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Order Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "200",
            description = "Order already created (idempotent response)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error or insufficient stock",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Order details including items and shipping info", required = true)
            @Valid @RequestBody OrderRequest request,
            @Parameter(description = "Idempotency key to prevent duplicate orders (UUID recommended)", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        // Check idempotency cache if key provided
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            OrderResponse cachedResponse = idempotencyCache.get(idempotencyKey);
            if (cachedResponse != null) {
                return ResponseEntity.status(HttpStatus.OK).body(cachedResponse);
            }
        }
        
        OrderResponse response = orderFacade.createOrder(request);
        
        // Store in idempotency cache if key provided
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyCache.put(idempotencyKey, response);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "List User Orders",
        description = """
            List all orders for authenticated user. Returns lightweight OrderListResponse (55% smaller than OrderResponse).
            
            **Features:**
            - Lists user's order history
            - Sorted by creation date (newest first)
            - Lightweight DTOs for performance
            - Includes order status and totals
            
            **Security:** Requires authentication - returns only user's own orders
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Order Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderListResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<List<OrderListResponse>> listOrders(
            @AuthenticationPrincipal String userId) {
        // List orders for authenticated user - lightweight DTOs (55% smaller)
        List<OrderListResponse> orders = orderFacade.listOrdersByCustomer(userId);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @Operation(
        summary = "Get Order Details",
        description = """
            Retrieve complete order details by order ID. Includes items, shipping, payments, and status history.
            
            **Features:**
            - Full order information
            - Item details with prices
            - Shipping and tracking info
            - Payment status
            - Order status history
            
            **Security:** Requires authentication and ownership verification
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Order Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Order does not belong to user",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{orderId}")
    @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID", required = true, example = "ORD-123456")
            @PathVariable String orderId) {
        OrderResponse response = orderFacade.getOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{orderId}/pay")
    @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
    public ResponseEntity<OrderResponse> payOrder(
            @PathVariable String orderId,
            @RequestParam String paymentMethod,
            @RequestParam String paymentToken,
            @RequestParam double amount) {
        OrderResponse response = orderFacade.payOrder(orderId, paymentMethod, paymentToken, amount);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{orderId}/ship")
    @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
    public ResponseEntity<OrderResponse> shipOrder(
            @PathVariable String orderId,
            @RequestParam String trackingNumber,
            @RequestParam String carrier) {
        OrderResponse response = orderFacade.shipOrder(orderId, trackingNumber, carrier);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{orderId}/deliver")
    @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable String orderId) {
        OrderResponse response = orderFacade.deliverOrder(orderId, java.time.LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "Cancel Order",
        description = """
            Cancel an existing order before shipment. Releases reserved stock.
            
            **Features:**
            - Cancels pending or paid orders
            - Releases product stock reservations
            - Initiates refund for paid orders
            - Records cancellation reason
            
            **Constraints:** Can only cancel orders before shipment
            
            **Security:** Requires authentication and ownership verification
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Order Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Order cannot be cancelled (already shipped/delivered)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Order does not belong to user",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/{orderId}/cancel")
    @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID", required = true, example = "ORD-123456")
            @PathVariable String orderId,
            @Parameter(description = "Reason for cancellation", required = true, example = "Customer changed mind")
            @RequestParam String reason) {
        OrderResponse response = orderFacade.cancelOrder(orderId, reason);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "Calculate Tax",
        description = """
            Calculate tax for order preview before checkout. Public endpoint for tax estimation.
            
            **Features:**
            - Calculates sales tax based on location
            - Supports multiple tax jurisdictions
            - Returns tax breakdown by item
            - No authentication required (preview only)
            
            **Use Case:** Display tax estimate before order creation
            
            **Security:** Public endpoint
            """,
        tags = {"Order Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tax calculated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaxCalculation.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "subtotal": 99.99,
                          "taxAmount": 8.50,
                          "taxRate": 0.085,
                          "total": 108.49,
                          "jurisdiction": "CA",
                          "itemTaxes": [
                            {
                              "productId": "PROD-123",
                              "taxAmount": 8.50
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid tax calculation request",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/calculate-tax")
    public ResponseEntity<TaxCalculation> calculateTax(
            @Parameter(description = "Tax calculation request with items and shipping address", required = true)
            @Valid @RequestBody TaxCalculationRequest request) {
        TaxCalculation taxCalculation = taxCalculationPort.calculateTax(request);
        return ResponseEntity.status(HttpStatus.OK).body(taxCalculation);
    }
}
