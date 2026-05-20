package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {

    private Long id;
    private String itemName;
    private String category;
    private Integer quantityAvailable;
    private Integer lowStockThreshold;
    private BigDecimal unitPrice;
    private InventoryStatus status;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
