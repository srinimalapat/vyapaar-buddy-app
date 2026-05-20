package com.vyapaarbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private BigDecimal todayTotalSales;
    private BigDecimal todayCashSales;
    private BigDecimal todayCreditSales;
    private BigDecimal todayUpiSales;
    private BigDecimal todayCardSales;

    private Long totalCustomers;
    private Long totalInventoryItems;

    private BigDecimal totalPendingUdhaar;
    private Long customersWithPendingCredit;

    private Long lowStockCount;

    private BigDecimal monthlySalesTotal;

    private List<SaleResponse> recentSales;
    private List<CreditTransactionResponse> recentCreditPayments;
}
