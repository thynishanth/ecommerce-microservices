package com.ecommerce.order.client;

import com.ecommerce.order.dto.InventoryResponse;
import com.ecommerce.order.dto.InventoryUpdateRequest;
import com.ecommerce.order.dto.InventoryUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    /**
     * Get inventory details for a product
     */
    public InventoryResponse getInventory(Long productId) {
        String url = inventoryServiceUrl + "/inventory/{productId}";
        return restTemplate.getForObject(url, InventoryResponse.class, productId);
    }

    /**
     * Check if inventory is available for a product
     */
    public Boolean checkAvailability(Long productId, int quantity) {
        String url = inventoryServiceUrl + "/inventory/{productId}/availability?quantity={quantity}";
        return restTemplate.getForObject(url, Boolean.class, productId, quantity);
    }

    /**
     * Update inventory after order is placed
     */
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
        String url = inventoryServiceUrl + "/inventory/update";
        return restTemplate.postForObject(url, request, InventoryUpdateResponse.class);
    }
}
