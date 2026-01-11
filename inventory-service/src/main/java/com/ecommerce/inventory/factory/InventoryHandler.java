package com.ecommerce.inventory.factory;

import com.ecommerce.inventory.entity.InventoryBatch;

import java.util.List;

/**
 * Interface for inventory handling strategies.
 * This follows the Factory Design Pattern to allow future extensibility
 * of inventory handling logic (e.g., FIFO, LIFO, custom strategies).
 */
public interface InventoryHandler {

    /**
     * Get the handler type identifier
     */
    String getType();

    /**
     * Sort batches according to the handler's strategy
     */
    List<InventoryBatch> sortBatches(List<InventoryBatch> batches);

    /**
     * Reserve inventory from batches for an order
     * Returns the list of batch IDs that were used for reservation
     */
    List<Long> reserveInventory(List<InventoryBatch> batches, int requiredQuantity);
}
