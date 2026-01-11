package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long productId;
    private String productName;
    private List<BatchDto> batches;
}
