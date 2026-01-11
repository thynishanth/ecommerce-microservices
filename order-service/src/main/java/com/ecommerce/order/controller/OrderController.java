package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders and order processing")
public class OrderController {

    private final OrderService orderService;

    /**
     * Place a new order
     */
    @Operation(
            summary = "Place a new order",
            description = "Creates a new order for specified products and quantities. Validates inventory availability before placing the order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order successfully placed",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or insufficient inventory",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order request containing product IDs and quantities",
                    required = true
            )
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);

        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order by ID
     */
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves detailed information about a specific order"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @Parameter(description = "ID of the order", required = true)
            @PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all orders
     */
    @Operation(
            summary = "Get all orders",
            description = "Retrieves a list of all orders in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all orders",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
