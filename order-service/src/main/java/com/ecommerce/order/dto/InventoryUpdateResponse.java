package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateResponse {
    private boolean success;
    private String message;
    private List<Long> updatedBatchIds;
}
