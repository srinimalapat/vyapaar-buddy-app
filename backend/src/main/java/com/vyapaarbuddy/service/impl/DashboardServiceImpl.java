package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.response.CreditTransactionResponse;
import com.vyapaarbuddy.dto.response.DashboardResponse;
import com.vyapaarbuddy.dto.response.SaleResponse;
import com.vyapaarbuddy.enums.CreditTransactionType;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.mapper.CreditMapper;
import com.vyapaarbuddy.mapper.SaleMapper;
import com.vyapaarbuddy.repository.CreditTransactionRepository;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.repository.SaleRepository;
import com.vyapaarbuddy.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SaleMapper saleMapper;
    private final CreditMapper creditMapper;

    @Override
    public DashboardResponse getDashboardStats(Long businessId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        // Today's sales breakdown by type
        var todaySales = saleRepository.findByBusinessIdAndSaleDate(businessId, today);
        BigDecimal todayTotal = BigDecimal.ZERO;
        BigDecimal todayCash = BigDecimal.ZERO;
        BigDecimal todayCredit = BigDecimal.ZERO;
        BigDecimal todayUpi = BigDecimal.ZERO;
        BigDecimal todayCard = BigDecimal.ZERO;
        for (var s : todaySales) {
            BigDecimal amt = s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO;
            todayTotal = todayTotal.add(amt);
            if (SaleType.CASH.equals(s.getType()))        todayCash   = todayCash.add(amt);
            else if (SaleType.CREDIT.equals(s.getType())) todayCredit = todayCredit.add(amt);
            else if (SaleType.UPI.equals(s.getType()))    todayUpi    = todayUpi.add(amt);
            else if (SaleType.CARD.equals(s.getType()))   todayCard   = todayCard.add(amt);
        }

        // Monthly sales total
        BigDecimal monthlySalesTotal = safe(
                saleRepository.sumTotalByBusinessIdAndDateRange(businessId, monthStart, monthEnd));

        // Udhaar stats from customer credit balances
        BigDecimal totalPendingUdhaar = safe(
                customerRepository.sumOutstandingCreditByBusinessId(businessId));
        Long customersWithPendingCredit = customerRepository.countCustomersWithOutstandingCredit(businessId);

        // Counts
        long totalCustomers     = customerRepository.findByBusinessId(businessId).size();
        long totalInventoryItems = inventoryItemRepository.findByBusinessId(businessId).size();

        // Low stock
        long lowStockCount = inventoryItemRepository.findActiveLowStockItems(businessId).size();

        // Recent sales (top 5)
        List<SaleResponse> recentSales = saleRepository
                .findTop5ByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream().map(saleMapper::toResponse).toList();

        // Recent credit payments (top 5)
        List<CreditTransactionResponse> recentCreditPayments = creditTransactionRepository
                .findTop5ByBusinessIdAndTypeOrderByCreatedAtDesc(businessId, CreditTransactionType.PAYMENT_RECEIVED)
                .stream().map(creditMapper::toResponse).toList();

        return DashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .totalInventoryItems(totalInventoryItems)
                .todayTotalSales(todayTotal)
                .todayCashSales(todayCash)
                .todayCreditSales(todayCredit)
                .todayUpiSales(todayUpi)
                .todayCardSales(todayCard)
                .totalPendingUdhaar(totalPendingUdhaar)
                .customersWithPendingCredit(customersWithPendingCredit != null ? customersWithPendingCredit : 0L)
                .lowStockCount(lowStockCount)
                .monthlySalesTotal(monthlySalesTotal)
                .recentSales(recentSales)
                .recentCreditPayments(recentCreditPayments)
                .build();
    }

    private BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
