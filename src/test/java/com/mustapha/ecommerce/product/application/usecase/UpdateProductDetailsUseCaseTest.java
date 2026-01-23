package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.UpdateProductDetailsCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.event.ProductDetailsUpdatedEvent;
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
 * UpdateProductDetailsUseCase Tests
 * Tests: Product details update, event publishing, not found handling
 */
@ExtendWith(MockitoExtension.class)
class UpdateProductDetailsUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private UpdateProductDetailsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductDetailsUseCase(productRepository, eventPublisher);
    }

    @Test
    void shouldUpdateProductDetails() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Old Name",
            "Old Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        UpdateProductDetailsCommand command = new UpdateProductDetailsCommand(
            productId, "New Name", "New Description"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.execute(command);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    void shouldPublishProductDetailsUpdatedEvent() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Old Name",
            "Old Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );
        product.clearDomainEvents(); // Clear creation event

        UpdateProductDetailsCommand command = new UpdateProductDetailsCommand(
            productId, "New Name", "New Description"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        ArgumentCaptor<ProductDomainEvent> eventCaptor = ArgumentCaptor.forClass(ProductDomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        ProductDomainEvent event = eventCaptor.getValue();
        assertTrue(event instanceof ProductDetailsUpdatedEvent);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        ProductId productId = ProductId.generate();
        UpdateProductDetailsCommand command = new UpdateProductDetailsCommand(
            productId, "New Name", "New Description"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ProductNotFoundException.class, () -> {
            useCase.execute(command);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldClearDomainEventsAfterPublishing() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Old Name",
            "Old Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        UpdateProductDetailsCommand command = new UpdateProductDetailsCommand(
            productId, "New Name", "New Description"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.execute(command);

        assertEquals(0, result.getDomainEvents().size());
    }
}
