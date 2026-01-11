package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.InventoryUpdateRequest;
import com.ecommerce.inventory.dto.InventoryUpdateResponse;
import com.ecommerce.inventory.entity.InventoryBatch;
import com.ecommerce.inventory.factory.FifoInventoryHandler;
import com.ecommerce.inventory.factory.InventoryHandlerFactory;
import com.ecommerce.inventory.repository.InventoryBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private InventoryHandlerFactory inventoryHandlerFactory;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private FifoInventoryHandler fifoHandler;

    @BeforeEach
    void setUp() {
        fifoHandler = new FifoInventoryHandler();
    }

    @Test
    void getInventoryByProductId_ShouldReturnSortedBatches() {
        // Given
        Long productId = 1001L;
        List<InventoryBatch> batches = Arrays.asList(
                new InventoryBatch(1L, productId, "Laptop", 50, LocalDate.of(2026, 6, 25)),
                new InventoryBatch(2L, productId, "Laptop", 30, LocalDate.of(2026, 3, 15))
        );

        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId))
                .thenReturn(batches);
        when(inventoryHandlerFactory.getDefaultHandler()).thenReturn(fifoHandler);

        // When
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals("Laptop", response.getProductName());
        assertEquals(2, response.getBatches().size());
        // Verify sorted by expiry date
        assertTrue(response.getBatches().get(0).getExpiryDate()
                .isBefore(response.getBatches().get(1).getExpiryDate()));
    }

    @Test
    void getInventoryByProductId_WhenNoBatches_ShouldReturnEmptyResponse() {
        // Given
        Long productId = 9999L;
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId))
                .thenReturn(Collections.emptyList());

        // When
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertNull(response.getProductName());
        assertTrue(response.getBatches().isEmpty());
    }

    @Test
    void updateInventory_ShouldSuccessfullyUpdateWhenSufficientStock() {
        // Given
        Long productId = 1001L;
        int requiredQuantity = 20;

        InventoryBatch batch1 = new InventoryBatch(1L, productId, "Laptop", 30, LocalDate.of(2026, 3, 15));
        InventoryBatch batch2 = new InventoryBatch(2L, productId, "Laptop", 50, LocalDate.of(2026, 6, 25));
        List<InventoryBatch> batches = Arrays.asList(batch1, batch2);

        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(productId);
        request.setQuantity(requiredQuantity);

        when(inventoryBatchRepository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(productId, 0))
                .thenReturn(batches);
        when(inventoryHandlerFactory.getDefaultHandler()).thenReturn(fifoHandler);
        when(inventoryBatchRepository.saveAll(any())).thenReturn(batches);

        // When
        InventoryUpdateResponse response = inventoryService.updateInventory(request);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Inventory updated successfully", response.getMessage());
        assertFalse(response.getUpdatedBatchIds().isEmpty());
        verify(inventoryBatchRepository).saveAll(any());
    }

    @Test
    void updateInventory_ShouldFailWhenInsufficientStock() {
        // Given
        Long productId = 1001L;
        int requiredQuantity = 100;

        InventoryBatch batch = new InventoryBatch(1L, productId, "Laptop", 30, LocalDate.of(2026, 3, 15));
        List<InventoryBatch> batches = Collections.singletonList(batch);

        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(productId);
        request.setQuantity(requiredQuantity);

        when(inventoryBatchRepository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(productId, 0))
                .thenReturn(batches);

        // When
        InventoryUpdateResponse response = inventoryService.updateInventory(request);

        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Insufficient inventory"));
        assertTrue(response.getUpdatedBatchIds().isEmpty());
        verify(inventoryBatchRepository, never()).saveAll(any());
    }

    @Test
    void checkAvailability_ShouldReturnTrueWhenSufficientStock() {
        // Given
        Long productId = 1001L;
        int requiredQuantity = 50;

        InventoryBatch batch = new InventoryBatch(1L, productId, "Laptop", 100, LocalDate.of(2026, 6, 25));
        when(inventoryBatchRepository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(productId, 0))
                .thenReturn(Collections.singletonList(batch));

        // When
        boolean available = inventoryService.checkAvailability(productId, requiredQuantity);

        // Then
        assertTrue(available);
    }

    @Test
    void checkAvailability_ShouldReturnFalseWhenInsufficientStock() {
        // Given
        Long productId = 1001L;
        int requiredQuantity = 150;

        InventoryBatch batch = new InventoryBatch(1L, productId, "Laptop", 100, LocalDate.of(2026, 6, 25));
        when(inventoryBatchRepository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(productId, 0))
                .thenReturn(Collections.singletonList(batch));

        // When
        boolean available = inventoryService.checkAvailability(productId, requiredQuantity);

        // Then
        assertFalse(available);
    }

    @Test
    void getTotalAvailableQuantity_ShouldReturnSumOfAllBatches() {
        // Given
        Long productId = 1001L;
        List<InventoryBatch> batches = Arrays.asList(
                new InventoryBatch(1L, productId, "Laptop", 50, LocalDate.of(2026, 6, 25)),
                new InventoryBatch(2L, productId, "Laptop", 30, LocalDate.of(2026, 3, 15))
        );

        when(inventoryBatchRepository.findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(productId, 0))
                .thenReturn(batches);

        // When
        int total = inventoryService.getTotalAvailableQuantity(productId);

        // Then
        assertEquals(80, total);
    }
}
