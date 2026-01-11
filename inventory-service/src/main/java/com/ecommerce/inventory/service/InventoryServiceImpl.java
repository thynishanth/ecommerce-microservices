package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.InventoryBatch;
import com.ecommerce.inventory.factory.InventoryHandler;
import com.ecommerce.inventory.factory.InventoryHandlerFactory;
import com.ecommerce.inventory.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryHandlerFactory inventoryHandlerFactory;

    @Override
    public InventoryResponse getInventoryByProductId(Long productId) {
        InventoryHandler handler = inventoryHandlerFactory.getDefaultHandler();
        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);

        if (batches.isEmpty()) {
            return InventoryResponse.builder()
                    .productId(productId)
                    .productName(null)
                    .batches(new ArrayList<>())
                    .build();
        }

        List<InventoryBatch> sortedBatches = handler.sortBatches(batches);
        String productName = batches.get(0).getProductName();

        List<BatchDto> batchDtos = sortedBatches.stream()
                .map(batch -> BatchDto.builder()
                        .batchId(batch.getBatchId())
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .build())
                .collect(Collectors.toList());

        return InventoryResponse.builder()
                .productId(productId)
                .productName(productName)
                .batches(batchDtos)
                .build();
    }

    @Override
    @Transactional
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
        Long productId = request.getProductId();
        int requiredQuantity = request.getQuantity();

        // Get available batches with quantity > 0
        List<InventoryBatch> availableBatches = inventoryBatchRepository
                .findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(productId, 0);

        // Check total available quantity
        int totalAvailable = availableBatches.stream()
                .mapToInt(InventoryBatch::getQuantity)
                .sum();

        if (totalAvailable < requiredQuantity) {
            return InventoryUpdateResponse.builder()
                    .success(false)
                    .message("Insufficient inventory. Available: " + totalAvailable + ", Required: " + requiredQuantity)
                    .updatedBatchIds(new ArrayList<>())
                    .build();
        }

        // Use factory to get handler and reserve inventory
        InventoryHandler handler = inventoryHandlerFactory.getDefaultHandler();
        List<Long> reservedBatchIds = handler.reserveInventory(availableBatches, requiredQuantity);

        // Save updated batches
        inventoryBatchRepository.saveAll(availableBatches);

        return InventoryUpdateResponse.builder()
                .success(true)
                .message("Inventory updated successfully")
                .updatedBatchIds(reservedBatchIds)
                .build();
    }

    @Override
    public boolean checkAvailability(Long productId, int requiredQuantity) {
        return getTotalAvailableQuantity(productId) >= requiredQuantity;
    }

    @Override
    public int getTotalAvailableQuantity(Long productId) {
        List<InventoryBatch> batches = inventoryBatchRepository
                .findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(productId, 0);
        return batches.stream()
                .mapToInt(InventoryBatch::getQuantity)
                .sum();
    }
}
