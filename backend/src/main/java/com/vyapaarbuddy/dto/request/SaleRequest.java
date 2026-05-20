package com.vyapaarbuddy.dto.request;

import com.vyapaarbuddy.enums.SaleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaleRequest {

    private Long customerId;

    @NotNull(message = "Sale type is required")
    private SaleType saleType;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Paid amount cannot be negative")
    private BigDecimal paidAmount;

    private LocalDate saleDate;

    private String notes;

    @NotEmpty(message = "At least one sale item is required")
    @Valid
    private List<SaleItemRequest> items;
}
