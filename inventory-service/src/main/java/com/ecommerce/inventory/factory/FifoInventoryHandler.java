package com.ecommerce.inventory.factory;

import com.ecommerce.inventory.entity.InventoryBatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FIFO (First Expiry First Out) inventory handler.
 * Prioritizes batches with earlier expiry dates to minimize waste.
 */
@Component
public class FifoInventoryHandler implements InventoryHandler {

    public static final String TYPE = "FIFO";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<InventoryBatch> sortBatches(List<InventoryBatch> batches) {
        return batches.stream()
                .sorted(Comparator.comparing(InventoryBatch::getExpiryDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> reserveInventory(List<InventoryBatch> batches, int requiredQuantity) {
        List<Long> reservedBatchIds = new ArrayList<>();
        int remainingQuantity = requiredQuantity;

        // Sort batches by expiry date (FIFO - First Expiry First Out)
        List<InventoryBatch> sortedBatches = sortBatches(batches);

        for (InventoryBatch batch : sortedBatches) {
            if (remainingQuantity <= 0) {
                break;
            }

            if (batch.getQuantity() > 0) {
                int quantityToReserve = Math.min(batch.getQuantity(), remainingQuantity);
                batch.setQuantity(batch.getQuantity() - quantityToReserve);
                remainingQuantity -= quantityToReserve;
                reservedBatchIds.add(batch.getBatchId());
            }
        }

        return reservedBatchIds;
    }
}
