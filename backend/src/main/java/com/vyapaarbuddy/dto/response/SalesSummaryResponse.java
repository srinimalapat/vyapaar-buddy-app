package com.vyapaarbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryResponse {

    private LocalDate date;
    private Integer year;
    private Integer month;
    private BigDecimal totalSales;
    private BigDecimal cashSales;
    private BigDecimal creditSales;
    private BigDecimal upiSales;
    private BigDecimal cardSales;
    private BigDecimal totalPaid;
    private BigDecimal totalBalance;
    private Integer saleCount;
}
