package com.vyapaarbuddy.mapper;

import com.vyapaarbuddy.dto.response.CreditTransactionResponse;
import com.vyapaarbuddy.entity.CreditTransaction;
import org.springframework.stereotype.Component;

@Component
public class CreditMapper {

    public CreditTransactionResponse toResponse(CreditTransaction entity) {
        if (entity == null) return null;
        return CreditTransactionResponse.builder()
                .id(entity.getId())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .customerName(entity.getCustomer() != null ? entity.getCustomer().getName() : null)
                .transactionType(entity.getType())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .transactionDate(entity.getTransactionDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
