package com.ecommerce.order.service;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Long productId = request.getProductId();
        int quantity = request.getQuantity();

        log.info("Placing order for product {} with quantity {}", productId, quantity);

        // Check inventory availability
        Boolean isAvailable = inventoryClient.checkAvailability(productId, quantity);

        if (isAvailable == null || !isAvailable) {
            log.warn("Insufficient inventory for product {}", productId);
            return OrderResponse.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .status("FAILED")
                    .reservedFromBatchIds(new ArrayList<>())
                    .message("Insufficient inventory available")
                    .build();
        }

        // Get product details from inventory
        InventoryResponse inventoryResponse = inventoryClient.getInventory(productId);
        String productName = inventoryResponse != null ? inventoryResponse.getProductName() : null;

        // Update inventory
        InventoryUpdateRequest updateRequest = new InventoryUpdateRequest();
        updateRequest.setProductId(productId);
        updateRequest.setQuantity(quantity);

        InventoryUpdateResponse updateResponse = inventoryClient.updateInventory(updateRequest);

        if (!updateResponse.isSuccess()) {
            log.error("Failed to update inventory for product {}: {}", productId, updateResponse.getMessage());
            return OrderResponse.builder()
                    .productId(productId)
                    .productName(productName)
                    .quantity(quantity)
                    .status("FAILED")
                    .reservedFromBatchIds(new ArrayList<>())
                    .message(updateResponse.getMessage())
                    .build();
        }

        // Create and save the order
        String batchIdsString = updateResponse.getUpdatedBatchIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        Order order = Order.builder()
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .status(OrderStatus.PLACED)
                .orderDate(LocalDate.now())
                .reservedBatchIds(batchIdsString)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with ID {}", savedOrder.getOrderId());

        return OrderResponse.builder()
                .orderId(savedOrder.getOrderId())
                .productId(savedOrder.getProductId())
                .productName(savedOrder.getProductName())
                .quantity(savedOrder.getQuantity())
                .status(savedOrder.getStatus().name())
                .reservedFromBatchIds(updateResponse.getUpdatedBatchIds())
                .message("Order placed. Inventory reserved.")
                .build();
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
