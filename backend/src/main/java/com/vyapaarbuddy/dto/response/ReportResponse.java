package com.vyapaarbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private String reportType;
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
    private Long saleCount;

    private BigDecimal totalOutstandingCredit;
    private Long customersWithPendingCredit;
    private Long lowStockCount;

    private List<SaleResponse> sales;
    private List<CustomerResponse> customers;
    private List<InventoryItemResponse> inventoryItems;
}
