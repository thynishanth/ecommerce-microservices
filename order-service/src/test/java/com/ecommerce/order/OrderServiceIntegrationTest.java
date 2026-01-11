package com.ecommerce.order;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryClient inventoryClient;

    @Test
    void placeOrder_ShouldCreateOrderSuccessfully() throws Exception {
        // Given
        OrderRequest request = new OrderRequest(1002L, 3);

        InventoryResponse inventoryResponse = InventoryResponse.builder()
                .productId(1002L)
                .productName("Smartphone")
                .batches(Collections.emptyList())
                .build();

        InventoryUpdateResponse updateResponse = InventoryUpdateResponse.builder()
                .success(true)
                .message("Inventory updated successfully")
                .updatedBatchIds(Arrays.asList(9L))
                .build();

        when(inventoryClient.checkAvailability(1002L, 3)).thenReturn(true);
        when(inventoryClient.getInventory(1002L)).thenReturn(inventoryResponse);
        when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);

        // When & Then
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1002))
                .andExpect(jsonPath("$.productName").value("Smartphone"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.message").value("Order placed. Inventory reserved."));
    }

    @Test
    void placeOrder_ShouldFailWhenInventoryNotAvailable() throws Exception {
        // Given
        OrderRequest request = new OrderRequest(1002L, 1000);

        when(inventoryClient.checkAvailability(1002L, 1000)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Insufficient inventory available"));
    }

    @Test
    void getExistingOrders_ShouldReturnPreloadedOrders() throws Exception {
        // Test getting pre-loaded orders from Liquibase
        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10)); // 10 orders loaded from CSV
    }

    @Test
    void getOrderById_ShouldReturnOrderFromPreloadedData() throws Exception {
        // Test getting order with ID 1 (from CSV data)
        mockMvc.perform(get("/order/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productId").value(1005))
                .andExpect(jsonPath("$.productName").value("Smartwatch"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void getOrderById_ShouldReturnNotFoundForNonExistentOrder() throws Exception {
        mockMvc.perform(get("/order/{orderId}", 9999L))
                .andExpect(status().isNotFound());
    }
}
