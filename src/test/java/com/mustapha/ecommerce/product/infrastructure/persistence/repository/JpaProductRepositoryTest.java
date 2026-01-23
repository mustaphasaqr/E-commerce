package com.mustapha.ecommerce.product.infrastructure.persistence.repository;

import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JpaProductRepository Tests
 * Tests: Repository adapter, Spring Data integration, mapper delegation
 */
@ExtendWith(MockitoExtension.class)
class JpaProductRepositoryTest {

    @Mock
    private SpringDataProductRepository springDataRepository;

    @Mock
    private ProductMapper mapper;

    private JpaProductRepository repository;

    private Product testProduct;
    private ProductJpaEntity testEntity;

    @BeforeEach
    void setUp() {
        repository = new JpaProductRepository(springDataRepository, mapper);

        testProduct = Product.create(
            SKU.of("TEST-SKU"),
            "Test Product",
            "Test Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        testEntity = createTestEntity();
    }

    private ProductJpaEntity createTestEntity() {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId("550e8400-e29b-41d4-a716-446655440000");
        entity.setSku("TEST-SKU");
        entity.setName("Test Product");
        entity.setDescription("Test Description");
        entity.setPrice(new BigDecimal("99.99"));
        entity.setCurrency("USD");
        entity.setTotalStock(100);
        entity.setAvailableStock(100);
        entity.setReservedStock(0);
        entity.setActive(true);
        entity.setVisible(true);
        entity.setAvailableForPurchase(true);
        entity.setDiscontinued(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setReservations(new HashMap<>());
        entity.setVersion(1L);
        return entity;
    }

    @Test
    void shouldSaveProduct() {
        when(mapper.toEntity(testProduct)).thenReturn(testEntity);
        when(springDataRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testProduct);

        Product result = repository.save(testProduct);

        assertNotNull(result);
        verify(mapper).toEntity(testProduct);
        verify(springDataRepository).save(testEntity);
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void shouldFindProductById() {
        ProductId productId = ProductId.of("550e8400-e29b-41d4-a716-446655440000");
        when(springDataRepository.findById("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testProduct);

        Optional<Product> result = repository.findById(productId);

        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(springDataRepository).findById("550e8400-e29b-41d4-a716-446655440000");
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void shouldReturnEmptyWhenProductNotFoundById() {
        ProductId productId = ProductId.of("650e8400-e29b-41d4-a716-446655440000");
        when(springDataRepository.findById("650e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.empty());

        Optional<Product> result = repository.findById(productId);

        assertFalse(result.isPresent());
        verify(springDataRepository).findById("650e8400-e29b-41d4-a716-446655440000");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void shouldFindProductBySku() {
        SKU sku = SKU.of("TEST-SKU");
        when(springDataRepository.findBySku("TEST-SKU")).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testProduct);

        Optional<Product> result = repository.findBySku(sku);

        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(springDataRepository).findBySku("TEST-SKU");
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void shouldReturnEmptyWhenProductNotFoundBySku() {
        SKU sku = SKU.of("NON-EXISTENT");
        when(springDataRepository.findBySku("NON-EXISTENT")).thenReturn(Optional.empty());

        Optional<Product> result = repository.findBySku(sku);

        assertFalse(result.isPresent());
        verify(springDataRepository).findBySku("NON-EXISTENT");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void shouldCheckIfProductExistsById() {
        ProductId productId = ProductId.of("550e8400-e29b-41d4-a716-446655440000");
        when(springDataRepository.existsById("550e8400-e29b-41d4-a716-446655440000")).thenReturn(true);

        boolean exists = repository.existsById(productId);

        assertTrue(exists);
        verify(springDataRepository).existsById("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void shouldReturnFalseWhenProductDoesNotExistById() {
        ProductId productId = ProductId.of("750e8400-e29b-41d4-a716-446655440000");
        when(springDataRepository.existsById("750e8400-e29b-41d4-a716-446655440000")).thenReturn(false);

        boolean exists = repository.existsById(productId);

        assertFalse(exists);
        verify(springDataRepository).existsById("750e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void shouldCheckIfProductExistsBySku() {
        SKU sku = SKU.of("TEST-SKU");
        when(springDataRepository.existsBySku("TEST-SKU")).thenReturn(true);

        boolean exists = repository.existsBySku(sku);

        assertTrue(exists);
        verify(springDataRepository).existsBySku("TEST-SKU");
    }

    @Test
    void shouldReturnFalseWhenProductDoesNotExistBySku() {
        SKU sku = SKU.of("NON-EXISTENT");
        when(springDataRepository.existsBySku("NON-EXISTENT")).thenReturn(false);

        boolean exists = repository.existsBySku(sku);

        assertFalse(exists);
        verify(springDataRepository).existsBySku("NON-EXISTENT");
    }

    @Test
    void shouldFindAllProducts() {
        ProductJpaEntity entity1 = createTestEntity();
        entity1.setId("850e8400-e29b-41d4-a716-446655440001");
        entity1.setSku("SKU-001");

        ProductJpaEntity entity2 = createTestEntity();
        entity2.setId("850e8400-e29b-41d4-a716-446655440002");
        entity2.setSku("SKU-002");

        Product product1 = Product.create(
            SKU.of("SKU-001"),
            "Product 1",
            "Desc 1",
            Price.of(new BigDecimal("10.00"), Currency.getInstance("USD")),
            Stock.of(10)
        );

        Product product2 = Product.create(
            SKU.of("SKU-002"),
            "Product 2",
            "Desc 2",
            Price.of(new BigDecimal("20.00"), Currency.getInstance("USD")),
            Stock.of(20)
        );

        when(springDataRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(product1);
        when(mapper.toDomain(entity2)).thenReturn(product2);

        List<Product> results = repository.findAll();

        assertEquals(2, results.size());
        assertTrue(results.contains(product1));
        assertTrue(results.contains(product2));
        verify(springDataRepository).findAll();
        verify(mapper, times(2)).toDomain(any(ProductJpaEntity.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoProducts() {
        when(springDataRepository.findAll()).thenReturn(Collections.emptyList());

        List<Product> results = repository.findAll();

        assertTrue(results.isEmpty());
        verify(springDataRepository).findAll();
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void shouldDeleteProductById() {
        ProductId productId = ProductId.of("550e8400-e29b-41d4-a716-446655440000");

        repository.deleteById(productId);

        verify(springDataRepository).deleteById("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void shouldCountProducts() {
        when(springDataRepository.count()).thenReturn(42L);

        long count = repository.count();

        assertEquals(42L, count);
        verify(springDataRepository).count();
    }

    @Test
    void shouldCountZeroWhenNoProducts() {
        when(springDataRepository.count()).thenReturn(0L);

        long count = repository.count();

        assertEquals(0L, count);
        verify(springDataRepository).count();
    }

    @Test
    void shouldPassProductIdValueToSpringData() {
        ProductId productId = ProductId.of("950e8400-e29b-41d4-a716-446655440000");
        when(springDataRepository.findById("950e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.empty());

        repository.findById(productId);

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        verify(springDataRepository).findById(idCaptor.capture());
        assertEquals("950e8400-e29b-41d4-a716-446655440000", idCaptor.getValue());
    }

    @Test
    void shouldPassSkuValueToSpringData() {
        SKU sku = SKU.of("CUSTOM-SKU-123");
        when(springDataRepository.findBySku("CUSTOM-SKU-123")).thenReturn(Optional.empty());

        repository.findBySku(sku);

        ArgumentCaptor<String> skuCaptor = ArgumentCaptor.forClass(String.class);
        verify(springDataRepository).findBySku(skuCaptor.capture());
        assertEquals("CUSTOM-SKU-123", skuCaptor.getValue());
    }

    @Test
    void shouldDelegateAllMappingToMapper() {
        when(mapper.toEntity(any(Product.class))).thenReturn(testEntity);
        when(springDataRepository.save(any(ProductJpaEntity.class))).thenReturn(testEntity);
        when(mapper.toDomain(any(ProductJpaEntity.class))).thenReturn(testProduct);

        repository.save(testProduct);

        verify(mapper).toEntity(testProduct);
        verify(mapper).toDomain(testEntity);
    }

    @Test
    void shouldPreserveEntityReturnedBySpringDataOnSave() {
        ProductJpaEntity savedEntity = createTestEntity();
        savedEntity.setVersion(2L); // Version incremented by database

        when(mapper.toEntity(testProduct)).thenReturn(testEntity);
        when(springDataRepository.save(testEntity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(testProduct);

        repository.save(testProduct);

        ArgumentCaptor<ProductJpaEntity> entityCaptor = ArgumentCaptor.forClass(ProductJpaEntity.class);
        verify(mapper).toDomain(entityCaptor.capture());
        assertEquals(savedEntity, entityCaptor.getValue());
    }

    @Test
    void shouldHandleMultipleConsecutiveSaves() {
        when(mapper.toEntity(testProduct)).thenReturn(testEntity);
        when(springDataRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testProduct);

        repository.save(testProduct);
        repository.save(testProduct);
        repository.save(testProduct);

        verify(mapper, times(3)).toEntity(testProduct);
        verify(springDataRepository, times(3)).save(testEntity);
        verify(mapper, times(3)).toDomain(testEntity);
    }

    @Test
    void shouldHandleMultipleConsecutiveFinds() {
        ProductId id = ProductId.of("550e8400-e29b-41d4-a716-446655440000");
        when(springDataRepository.findById("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testProduct);

        repository.findById(id);
        repository.findById(id);

        verify(springDataRepository, times(2)).findById("550e8400-e29b-41d4-a716-446655440000");
        verify(mapper, times(2)).toDomain(testEntity);
    }
}
