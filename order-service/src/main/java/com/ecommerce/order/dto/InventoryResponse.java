package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long productId;
    private String productName;
    private List<BatchDto> batches;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchDto {
        private Long batchId;
        private Integer quantity;
        private LocalDate expiryDate;
    }
}
