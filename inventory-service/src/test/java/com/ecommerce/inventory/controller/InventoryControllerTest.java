package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.BatchDto;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.InventoryUpdateRequest;
import com.ecommerce.inventory.dto.InventoryUpdateResponse;
import com.ecommerce.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void getInventory_ShouldReturnInventoryResponse() throws Exception {
        // Given
        Long productId = 1001L;
        InventoryResponse response = InventoryResponse.builder()
                .productId(productId)
                .productName("Laptop")
                .batches(Arrays.asList(
                        BatchDto.builder()
                                .batchId(1L)
                                .quantity(50)
                                .expiryDate(LocalDate.of(2025, 12, 31))
                                .build(),
                        BatchDto.builder()
                                .batchId(2L)
                                .quantity(30)
                                .expiryDate(LocalDate.of(2026, 3, 15))
                                .build()
                ))
                .build();

        when(inventoryService.getInventoryByProductId(productId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/inventory/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.batches").isArray())
                .andExpect(jsonPath("$.batches.length()").value(2))
                .andExpect(jsonPath("$.batches[0].batchId").value(1))
                .andExpect(jsonPath("$.batches[0].quantity").value(50));
    }

    @Test
    void updateInventory_ShouldReturnSuccessResponse() throws Exception {
        // Given
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1001L);
        request.setQuantity(20);

        InventoryUpdateResponse response = InventoryUpdateResponse.builder()
                .success(true)
                .message("Inventory updated successfully")
                .updatedBatchIds(Arrays.asList(1L, 2L))
                .build();

        when(inventoryService.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inventory updated successfully"))
                .andExpect(jsonPath("$.updatedBatchIds").isArray());
    }

    @Test
    void updateInventory_ShouldReturnBadRequestWhenFailed() throws Exception {
        // Given
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1001L);
        request.setQuantity(1000);

        InventoryUpdateResponse response = InventoryUpdateResponse.builder()
                .success(false)
                .message("Insufficient inventory")
                .updatedBatchIds(Collections.emptyList())
                .build();

        when(inventoryService.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void checkAvailability_ShouldReturnTrue() throws Exception {
        // Given
        Long productId = 1001L;
        int quantity = 50;

        when(inventoryService.checkAvailability(productId, quantity)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/inventory/{productId}/availability", productId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkAvailability_ShouldReturnFalse() throws Exception {
        // Given
        Long productId = 1001L;
        int quantity = 500;

        when(inventoryService.checkAvailability(productId, quantity)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/inventory/{productId}/availability", productId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
