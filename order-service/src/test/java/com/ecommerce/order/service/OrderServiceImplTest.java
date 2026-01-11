package com.ecommerce.order.service;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void placeOrder_ShouldSucceedWhenInventoryIsAvailable() {
        // Given
        Long productId = 1002L;
        int quantity = 3;
        OrderRequest request = new OrderRequest(productId, quantity);

        InventoryResponse inventoryResponse = InventoryResponse.builder()
                .productId(productId)
                .productName("Smartphone")
                .batches(Collections.emptyList())
                .build();

        InventoryUpdateResponse updateResponse = InventoryUpdateResponse.builder()
                .success(true)
                .message("Inventory updated successfully")
                .updatedBatchIds(Arrays.asList(3L))
                .build();

        Order savedOrder = Order.builder()
                .orderId(5012L)
                .productId(productId)
                .productName("Smartphone")
                .quantity(quantity)
                .status(OrderStatus.PLACED)
                .orderDate(LocalDate.now())
                .reservedBatchIds("3")
                .build();

        when(inventoryClient.checkAvailability(productId, quantity)).thenReturn(true);
        when(inventoryClient.getInventory(productId)).thenReturn(inventoryResponse);
        when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        OrderResponse response = orderService.placeOrder(request);

        // Then
        assertNotNull(response);
        assertEquals(5012L, response.getOrderId());
        assertEquals(productId, response.getProductId());
        assertEquals("Smartphone", response.getProductName());
        assertEquals(quantity, response.getQuantity());
        assertEquals("PLACED", response.getStatus());
        assertEquals(Arrays.asList(3L), response.getReservedFromBatchIds());
        assertEquals("Order placed. Inventory reserved.", response.getMessage());

        verify(inventoryClient).checkAvailability(productId, quantity);
        verify(inventoryClient).updateInventory(any(InventoryUpdateRequest.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldFailWhenInventoryIsNotAvailable() {
        // Given
        Long productId = 1002L;
        int quantity = 1000;
        OrderRequest request = new OrderRequest(productId, quantity);

        when(inventoryClient.checkAvailability(productId, quantity)).thenReturn(false);

        // When
        OrderResponse response = orderService.placeOrder(request);

        // Then
        assertNotNull(response);
        assertNull(response.getOrderId());
        assertEquals(productId, response.getProductId());
        assertEquals(quantity, response.getQuantity());
        assertEquals("FAILED", response.getStatus());
        assertEquals("Insufficient inventory available", response.getMessage());
        assertTrue(response.getReservedFromBatchIds().isEmpty());

        verify(inventoryClient).checkAvailability(productId, quantity);
        verify(inventoryClient, never()).updateInventory(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_ShouldFailWhenInventoryUpdateFails() {
        // Given
        Long productId = 1002L;
        int quantity = 3;
        OrderRequest request = new OrderRequest(productId, quantity);

        InventoryResponse inventoryResponse = InventoryResponse.builder()
                .productId(productId)
                .productName("Smartphone")
                .batches(Collections.emptyList())
                .build();

        InventoryUpdateResponse updateResponse = InventoryUpdateResponse.builder()
                .success(false)
                .message("Inventory update failed")
                .updatedBatchIds(Collections.emptyList())
                .build();

        when(inventoryClient.checkAvailability(productId, quantity)).thenReturn(true);
        when(inventoryClient.getInventory(productId)).thenReturn(inventoryResponse);
        when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);

        // When
        OrderResponse response = orderService.placeOrder(request);

        // Then
        assertNotNull(response);
        assertNull(response.getOrderId());
        assertEquals("FAILED", response.getStatus());
        assertEquals("Inventory update failed", response.getMessage());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        // Given
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .productId(1005L)
                .productName("Smartwatch")
                .quantity(10)
                .status(OrderStatus.DELIVERED)
                .orderDate(LocalDate.of(2025, 12, 4))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        Optional<Order> result = orderService.getOrderById(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getOrderId());
        assertEquals(1005L, result.get().getProductId());
    }

    @Test
    void getOrderById_ShouldReturnEmptyWhenNotFound() {
        // Given
        Long orderId = 9999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.getOrderById(orderId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        Order order1 = Order.builder().orderId(1L).productId(1005L).build();
        Order order2 = Order.builder().orderId(2L).productId(1003L).build();

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // When
        var orders = orderService.getAllOrders();

        // Then
        assertEquals(2, orders.size());
    }
}
