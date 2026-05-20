package com.vyapaarbuddy.mapper;

import com.vyapaarbuddy.dto.response.SaleItemResponse;
import com.vyapaarbuddy.dto.response.SaleResponse;
import com.vyapaarbuddy.entity.Sale;
import com.vyapaarbuddy.entity.SaleItem;
import com.vyapaarbuddy.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SaleMapper {

    public SaleResponse toResponse(Sale entity) {
        if (entity == null) return null;

        List<SaleItemResponse> items = entity.getItems() == null ? Collections.emptyList() :
                entity.getItems().stream().map(this::toItemResponse).toList();

        return SaleResponse.builder()
                .id(entity.getId())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .customerName(entity.getCustomer() != null ? entity.getCustomer().getName() : null)
                .saleType(entity.getType())
                .totalAmount(entity.getTotalAmount())
                .paidAmount(entity.getPaidAmount())
                .balanceAmount(MoneyUtil.calculateBalance(entity.getTotalAmount(), entity.getPaidAmount()))
                .saleDate(entity.getSaleDate())
                .notes(entity.getNotes())
                .items(items)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SaleItemResponse toItemResponse(SaleItem item) {
        return SaleItemResponse.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
