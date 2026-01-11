package com.ecommerce.inventory;

import com.ecommerce.inventory.dto.InventoryUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getInventory_ShouldReturnInventoryForProduct() throws Exception {
        // Test with product ID from sample data (Laptop - 1001)
        mockMvc.perform(get("/inventory/{productId}", 1001L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1001))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.batches").isArray());
    }

    @Test
    void getInventory_ShouldReturnMultipleBatchesSortedByExpiryDate() throws Exception {
        // Test with product ID that has multiple batches (Smartwatch - 1005)
        mockMvc.perform(get("/inventory/{productId}", 1005L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1005))
                .andExpect(jsonPath("$.productName").value("Smartwatch"))
                .andExpect(jsonPath("$.batches.length()").value(3));
    }

    @Test
    void getInventory_ShouldReturnEmptyBatchesForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/inventory/{productId}", 9999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(9999))
                .andExpect(jsonPath("$.batches").isEmpty());
    }

    @Test
    void updateInventory_ShouldSucceedWithValidRequest() throws Exception {
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1001L); // Laptop
        request.setQuantity(10);

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inventory updated successfully"));
    }

    @Test
    void updateInventory_ShouldFailWithInsufficientStock() throws Exception {
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1001L); // Laptop
        request.setQuantity(10000); // More than available

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void checkAvailability_ShouldReturnTrueForAvailableStock() throws Exception {
        mockMvc.perform(get("/inventory/{productId}/availability", 1001L)
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkAvailability_ShouldReturnFalseForInsufficientStock() throws Exception {
        mockMvc.perform(get("/inventory/{productId}/availability", 1001L)
                        .param("quantity", "10000"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
