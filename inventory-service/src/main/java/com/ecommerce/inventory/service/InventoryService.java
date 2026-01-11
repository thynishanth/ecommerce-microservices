package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.InventoryBatch;

import java.util.List;

public interface InventoryService {

    /**
     * Get inventory batches for a product, sorted by expiry date
     */
    InventoryResponse getInventoryByProductId(Long productId);

    /**
     * Update inventory after an order is placed
     */
    InventoryUpdateResponse updateInventory(InventoryUpdateRequest request);

    /**
     * Check if sufficient inventory is available for a product
     */
    boolean checkAvailability(Long productId, int requiredQuantity);

    /**
     * Get total available quantity for a product
     */
    int getTotalAvailableQuantity(Long productId);
}
