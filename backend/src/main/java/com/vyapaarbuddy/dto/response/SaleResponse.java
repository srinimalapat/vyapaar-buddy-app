package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.SaleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private SaleType saleType;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private LocalDate saleDate;
    private String notes;
    private List<SaleItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
