package com.vyapaarbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalOutstandingCreditResponse {

    private BigDecimal totalOutstandingCredit;
    private Long customersWithPendingCredit;
}
