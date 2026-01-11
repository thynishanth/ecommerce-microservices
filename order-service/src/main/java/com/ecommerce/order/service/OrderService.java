package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    /**
     * Place a new order
     */
    OrderResponse placeOrder(OrderRequest request);

    /**
     * Get order by ID
     */
    Optional<Order> getOrderById(Long orderId);

    /**
     * Get all orders
     */
    List<Order> getAllOrders();
}
