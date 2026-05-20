package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.CreditTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private CreditTransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
}
