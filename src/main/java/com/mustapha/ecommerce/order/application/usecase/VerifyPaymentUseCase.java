package com.mustapha.ecommerce.order.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.VerifyPaymentCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentStatus;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentVerificationResult;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Verify Payment Use Case
 * Responsibility: Verify payment status and update order
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Verify payment status with payment gateway
 * 2. Find order by checkout ID (stored during initiation)
 * 3. Update order status based on payment result
 * 4. Publish domain events
 * 5. Send notifications
 * 
 * This is STEP 2 of the two-step payment flow:
 * - Step 1: Initiate → Get checkout URL
 * - Step 2: Verify → Confirm payment after customer returns
 */
@Component
public class VerifyPaymentUseCase {

    private static final Logger logger = LoggerFactory.getLogger(VerifyPaymentUseCase.class);
    
    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;
    private final DomainEventPublisher eventPublisher;
    private final NotificationPort notificationPort;

    public VerifyPaymentUseCase(
            OrderRepository orderRepository,
            PaymentPort paymentPort,
            DomainEventPublisher eventPublisher,
            NotificationPort notificationPort) {
        this.orderRepository = orderRepository;
        this.paymentPort = paymentPort;
        this.eventPublisher = eventPublisher;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public PaymentVerificationResult execute(VerifyPaymentCommand command) {
        // Step 1: Verify payment with gateway
        logger.info("Verifying payment for checkoutId: {}", command.getCheckoutId());
        
        PaymentVerificationResult result = paymentPort.verifyPayment(command.getCheckoutId());
        
        logger.info("Payment verification result: status={}, transactionId={}", 
                   result.status(), result.transactionId());
        
        // Step 2: Find order by checkout ID
        Order order = orderRepository.findByCheckoutId(command.getCheckoutId())
            .orElseThrow(() -> new IllegalStateException(
                "No order found for checkoutId: " + command.getCheckoutId() + 
                ". Please ensure order was created before verifying payment."));
        
        logger.info("Found order: {} for checkoutId: {}", 
                   order.getId().getValue(), command.getCheckoutId());
        
        // Step 3: Update order based on payment status
        if (result.status() == PaymentStatus.SUCCESS) {
            // Payment succeeded - mark order as paid
            order.markAsPaid();
            order.setTransactionId(result.transactionId());
            
            // Save and publish events
            Order savedOrder = orderRepository.save(order);
            savedOrder.getDomainEvents().forEach(eventPublisher::publish);
            savedOrder.clearDomainEvents();
            
            // Send notification
            notificationPort.sendPaymentReceived(order.getCustomerId(), order.getId());
            
            logger.info("✅ Payment verified and order marked as PAID: orderId={}, transactionId={}", 
                       order.getId().getValue(), result.transactionId());
            
        } else if (result.status() == PaymentStatus.FAILED) {
            // Payment failed - keep order in CONFIRMED state
            // Customer can try payment again
            logger.warn("❌ Payment failed for order: {}, reason: {}", 
                       order.getId().getValue(), result.message());
            
        } else if (result.status() == PaymentStatus.PENDING) {
            // Payment pending (e.g., bank transfer, manual review)
            // Keep order in current state, mark as pending in future enhancement
            logger.info("⏳ Payment pending for order: {}", order.getId().getValue());
            
        } else if (result.status() == PaymentStatus.CANCELLED) {
            // Customer cancelled payment
            logger.info("🚫 Payment cancelled by customer for order: {}", order.getId().getValue());
        }
        
        return result;
    }
}
