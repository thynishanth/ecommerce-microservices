package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void placeOrder_ShouldReturnCreatedResponse() throws Exception {
        // Given
        OrderRequest request = new OrderRequest(1002L, 3);

        OrderResponse response = OrderResponse.builder()
                .orderId(5012L)
                .productId(1002L)
                .productName("Smartphone")
                .quantity(3)
                .status("PLACED")
                .reservedFromBatchIds(Arrays.asList(3L))
                .message("Order placed. Inventory reserved.")
                .build();

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(5012))
                .andExpect(jsonPath("$.productId").value(1002))
                .andExpect(jsonPath("$.productName").value("Smartphone"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.reservedFromBatchIds[0]").value(3))
                .andExpect(jsonPath("$.message").value("Order placed. Inventory reserved."));
    }

    @Test
    void placeOrder_ShouldReturnBadRequestWhenFailed() throws Exception {
        // Given
        OrderRequest request = new OrderRequest(1002L, 1000);

        OrderResponse response = OrderResponse.builder()
                .productId(1002L)
                .quantity(1000)
                .status("FAILED")
                .reservedFromBatchIds(Collections.emptyList())
                .message("Insufficient inventory available")
                .build();

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Insufficient inventory available"));
    }

    @Test
    void getOrder_ShouldReturnOrderWhenFound() throws Exception {
        // Given
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .productId(1005L)
                .productName("Smartwatch")
                .quantity(10)
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDate.of(2025, 12, 4))
                .reservedBatchIds("1,2")
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        mockMvc.perform(get("/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productId").value(1005))
                .andExpect(jsonPath("$.productName").value("Smartwatch"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void getOrder_ShouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        // Given
        Long orderId = 9999L;
        when(orderService.getOrderById(orderId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/order/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() throws Exception {
        // Given
        Order order1 = Order.builder()
                .orderId(1L)
                .productId(1005L)
                .productName("Smartwatch")
                .quantity(10)
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDate.of(2025, 12, 4))
                .build();

        Order order2 = Order.builder()
                .orderId(2L)
                .productId(1003L)
                .productName("Tablet")
                .quantity(10)
                .status(OrderStatus.PLACED)
                .orderDate(LocalDate.of(2025, 12, 2))
                .build();

        when(orderService.getAllOrders()).thenReturn(Arrays.asList(order1, order2));

        // When & Then
        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[1].orderId").value(2));
    }
}
