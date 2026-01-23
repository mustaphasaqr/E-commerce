package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.ReserveStockCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.event.StockUpdatedEvent;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReserveStockUseCase Tests
 * Tests: Stock reservation, idempotency, event publishing, insufficient stock handling
 */
@ExtendWith(MockitoExtension.class)
class ReserveStockUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ReserveStockUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReserveStockUseCase(productRepository, eventPublisher);
    }

    @Test
    void shouldReserveStock() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );
        product.clearDomainEvents();

        ReserveStockCommand command = new ReserveStockCommand(productId, "ORDER-1", 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(90, result.getStock().getAvailableQuantity());
        assertEquals(10, result.getStock().getReservedQuantity());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    void shouldPublishStockUpdatedEvent() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );
        product.clearDomainEvents();

        ReserveStockCommand command = new ReserveStockCommand(productId, "ORDER-1", 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        ArgumentCaptor<ProductDomainEvent> eventCaptor = ArgumentCaptor.forClass(ProductDomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        ProductDomainEvent event = eventCaptor.getValue();
        assertTrue(event instanceof StockUpdatedEvent);
    }

    @Test
    void shouldBeIdempotentForSameOrderId() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );
        product.reserveStockForOrder("ORDER-1", 10);
        product.clearDomainEvents(); // Clear first reservation event

        ReserveStockCommand command = new ReserveStockCommand(productId, "ORDER-1", 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(90, result.getStock().getAvailableQuantity());
        assertEquals(10, result.getStock().getReservedQuantity());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        // No new event for idempotent operation
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldRejectReservationWhenInsufficientStock() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(5)
        );

        ReserveStockCommand command = new ReserveStockCommand(productId, "ORDER-1", 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Exception exception = assertThrows(com.mustapha.ecommerce.product.domain.exception.InsufficientStockException.class, () -> {
            useCase.execute(command);
        });

        assertTrue(exception.getMessage().contains("stock") || exception.getMessage().contains("Insufficient"));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        ProductId productId = ProductId.generate();
        ReserveStockCommand command = new ReserveStockCommand(productId, "ORDER-1", 10);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ProductNotFoundException.class, () -> {
            useCase.execute(command);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }
}
