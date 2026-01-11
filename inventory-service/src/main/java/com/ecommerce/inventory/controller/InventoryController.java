package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.InventoryUpdateRequest;
import com.ecommerce.inventory.dto.InventoryUpdateResponse;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing product inventory and stock levels")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Get inventory batches for a product, sorted by expiry date
     */
    @Operation(
            summary = "Get inventory for a product",
            description = "Retrieves all inventory batches for a specific product, sorted by expiry date"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved inventory",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId) {
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update inventory after an order is placed
     */
    @Operation(
            summary = "Update inventory",
            description = "Updates inventory quantities after an order is placed, using FIFO strategy based on expiry dates"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory successfully updated",
                    content = @Content(schema = @Schema(implementation = InventoryUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or insufficient inventory"
            )
    })
    @PostMapping("/update")
    public ResponseEntity<InventoryUpdateResponse> updateInventory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Inventory update request with product ID and quantity",
                    required = true
            )
            @Valid @RequestBody InventoryUpdateRequest request) {
        InventoryUpdateResponse response = inventoryService.updateInventory(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Check inventory availability for a product
     */
    @Operation(
            summary = "Check inventory availability",
            description = "Checks if a specific quantity of a product is available in inventory"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability check completed",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    @GetMapping("/{productId}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Quantity to check", required = true)
            @RequestParam int quantity) {
        boolean available = inventoryService.checkAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }
}
