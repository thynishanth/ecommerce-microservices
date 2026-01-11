package com.ecommerce.inventory.factory;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating and managing inventory handlers.
 * Follows the Factory Design Pattern to allow future extensibility
 * of inventory handling strategies.
 */
@Component
public class InventoryHandlerFactory {

    private final Map<String, InventoryHandler> handlers = new HashMap<>();
    private final InventoryHandler defaultHandler;

    public InventoryHandlerFactory(List<InventoryHandler> handlerList) {
        for (InventoryHandler handler : handlerList) {
            handlers.put(handler.getType(), handler);
        }
        // Set FIFO as default handler
        this.defaultHandler = handlers.get(FifoInventoryHandler.TYPE);
    }

    /**
     * Get an inventory handler by type.
     * Returns the default handler if type is not found.
     */
    public InventoryHandler getHandler(String type) {
        return handlers.getOrDefault(type, defaultHandler);
    }

    /**
     * Get the default inventory handler (FIFO).
     */
    public InventoryHandler getDefaultHandler() {
        return defaultHandler;
    }

    /**
     * Check if a handler type is supported.
     */
    public boolean isSupported(String type) {
        return handlers.containsKey(type);
    }
}
