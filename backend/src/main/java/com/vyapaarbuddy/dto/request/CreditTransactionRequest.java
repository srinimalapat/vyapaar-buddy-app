package com.vyapaarbuddy.dto.request;

import com.vyapaarbuddy.enums.CreditTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreditTransactionRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Transaction type is required")
    private CreditTransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String description;

    private LocalDate transactionDate;

    private Boolean allowOverPayment;
}
