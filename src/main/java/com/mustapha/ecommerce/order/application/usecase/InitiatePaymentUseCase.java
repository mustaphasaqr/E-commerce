package com.mustapha.ecommerce.order.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.InitiatePaymentCommand;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.CheckoutResult;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Initiate Payment Use Case
 * Responsibility: Create payment checkout session
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Retrieve order from repository
 * 2. Validate order is in CONFIRMED state (ready for payment)
 * 3. Create checkout session via PaymentPort
 * 4. Store checkout ID in order (for tracking)
 * 5. Return checkout result with redirect URL
 * 
 * This is STEP 1 of the two-step payment flow:
 * - Step 1: Initiate → Get checkout URL
 * - Step 2: Verify → Confirm payment after customer returns
 */
@Component
public class InitiatePaymentUseCase {

    private static final Logger logger = LoggerFactory.getLogger(InitiatePaymentUseCase.class);
    
    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;

    public InitiatePaymentUseCase(
            OrderRepository orderRepository, 
            PaymentPort paymentPort) {
        this.orderRepository = orderRepository;
        this.paymentPort = paymentPort;
    }

    @Transactional
    public CheckoutResult execute(InitiatePaymentCommand command) {
        // Step 1: Retrieve order
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Order not found: " + command.getOrderId().getValue()));
        
        // Step 2: Validate order state
        if (order.getStatus() != com.mustapha.ecommerce.order.domain.model.OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                "Cannot initiate payment for order in state: " + order.getStatus() + 
                ". Order must be CONFIRMED");
        }
        
        logger.info("Initiating payment for order: {}, amount: {}, method: {}", 
                   order.getId().getValue(), 
                   order.getTotalAmount().getAmount(), 
                   command.getPaymentMethod());
        
        // Step 3: Create checkout session
        CheckoutResult result = paymentPort.createCheckout(
            order.getId(),
            order.getTotalAmount(),
            command.getPaymentMethod(),
            command.getCustomerEmail()
        );
        
        if (!result.success()) {
            logger.error("Checkout creation failed for order {}: {}", 
                        order.getId().getValue(), result.message());
            throw new IllegalStateException("Payment checkout failed: " + result.message());
        }
        
        // Step 4: Store checkout ID in order for tracking
        order.setCheckoutId(result.checkoutId());
        orderRepository.save(order);
        
        logger.info("✅ Payment checkout created: orderId={}, checkoutId={}, expiresIn={}s", 
                   order.getId().getValue(), result.checkoutId(), result.expiresInSeconds());
        
        return result;
    }
}
