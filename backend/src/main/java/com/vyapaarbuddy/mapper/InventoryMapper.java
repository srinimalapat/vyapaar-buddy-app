package com.vyapaarbuddy.mapper;

import com.vyapaarbuddy.dto.request.InventoryItemRequest;
import com.vyapaarbuddy.dto.response.InventoryItemResponse;
import com.vyapaarbuddy.entity.InventoryItem;
import com.vyapaarbuddy.enums.InventoryStatus;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryItem toEntity(InventoryItemRequest request) {
        return InventoryItem.builder()
                .name(request.getItemName())
                .category(request.getCategory())
                .quantity(request.getQuantityAvailable())
                .lowStockThreshold(request.getLowStockThreshold())
                .unitPrice(request.getUnitPrice())
                .status(request.getStatus() != null ? request.getStatus() : InventoryStatus.ACTIVE)
                .build();
    }

    public void updateEntity(InventoryItem item, InventoryItemRequest request) {
        item.setName(request.getItemName());
        item.setCategory(request.getCategory());
        item.setQuantity(request.getQuantityAvailable());
        item.setLowStockThreshold(request.getLowStockThreshold());
        item.setUnitPrice(request.getUnitPrice());
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
    }

    public InventoryItemResponse toResponse(InventoryItem entity) {
        if (entity == null) return null;
        boolean lowStock = entity.getQuantity() != null
                && entity.getLowStockThreshold() != null
                && entity.getQuantity() <= entity.getLowStockThreshold();
        return InventoryItemResponse.builder()
                .id(entity.getId())
                .itemName(entity.getName())
                .category(entity.getCategory())
                .quantityAvailable(entity.getQuantity())
                .lowStockThreshold(entity.getLowStockThreshold())
                .unitPrice(entity.getUnitPrice())
                .status(entity.getStatus())
                .lowStock(lowStock)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
