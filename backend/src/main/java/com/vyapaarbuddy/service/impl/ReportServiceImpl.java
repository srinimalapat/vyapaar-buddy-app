package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.dto.response.InventoryItemResponse;
import com.vyapaarbuddy.dto.response.ReportResponse;
import com.vyapaarbuddy.dto.response.SaleResponse;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.mapper.CustomerMapper;
import com.vyapaarbuddy.mapper.InventoryMapper;
import com.vyapaarbuddy.mapper.SaleMapper;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.repository.SaleRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SaleMapper saleMapper;
    private final CustomerMapper customerMapper;
    private final InventoryMapper inventoryMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ReportResponse getDailySalesReport(LocalDate date) {
        Long businessId = currentUserService.getCurrentBusinessId();
        LocalDate reportDate = date != null ? date : LocalDate.now();

        var sales = saleRepository.findByBusinessIdAndSaleDate(businessId, reportDate);
        return buildSalesReport("DAILY_SALES", sales, reportDate, null, null);
    }

    @Override
    public ReportResponse getMonthlySalesReport(int year, int month) {
        if (month < 1 || month > 12) throw new BadRequestException("Month must be between 1 and 12");
        if (year < 2000 || year > 2100) throw new BadRequestException("Year must be between 2000 and 2100");

        Long businessId = currentUserService.getCurrentBusinessId();
        YearMonth ym = YearMonth.of(year, month);
        var sales = saleRepository.findByBusinessIdAndSaleDateBetween(businessId, ym.atDay(1), ym.atEndOfMonth());
        return buildSalesReport("MONTHLY_SALES", sales, null, year, month);
    }

    @Override
    public ReportResponse getCustomerCreditReport() {
        Long businessId = currentUserService.getCurrentBusinessId();

        List<CustomerResponse> customers = customerRepository
                .findCustomersWithOutstandingCredit(businessId)
                .stream()
                .filter(c -> CustomerStatus.ACTIVE.equals(c.getStatus()))
                .map(customerMapper::toResponse)
                .toList();

        BigDecimal totalOutstanding = safe(customerRepository.sumOutstandingCreditByBusinessId(businessId));
        Long count = customerRepository.countCustomersWithOutstandingCredit(businessId);

        return ReportResponse.builder()
                .reportType("CUSTOMER_CREDIT")
                .totalOutstandingCredit(totalOutstanding)
                .customersWithPendingCredit(count != null ? count : 0L)
                .customers(customers)
                .build();
    }

    @Override
    public ReportResponse getInventoryLowStockReport() {
        Long businessId = currentUserService.getCurrentBusinessId();

        List<InventoryItemResponse> items = inventoryItemRepository
                .findActiveLowStockItems(businessId)
                .stream().map(inventoryMapper::toResponse).toList();

        return ReportResponse.builder()
                .reportType("INVENTORY_LOW_STOCK")
                .lowStockCount((long) items.size())
                .inventoryItems(items)
                .build();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ReportResponse buildSalesReport(String type, List<com.vyapaarbuddy.entity.Sale> sales,
                                             LocalDate date, Integer year, Integer month) {
        BigDecimal totalSales = BigDecimal.ZERO, cashSales = BigDecimal.ZERO,
                creditSales = BigDecimal.ZERO, upiSales = BigDecimal.ZERO,
                cardSales = BigDecimal.ZERO, totalPaid = BigDecimal.ZERO, totalBalance = BigDecimal.ZERO;

        for (var s : sales) {
            BigDecimal amt  = safe(s.getTotalAmount());
            BigDecimal paid = safe(s.getPaidAmount());
            totalSales  = totalSales.add(amt);
            totalPaid   = totalPaid.add(paid);
            totalBalance = totalBalance.add(amt.subtract(paid));
            if (SaleType.CASH.equals(s.getType()))        cashSales   = cashSales.add(amt);
            else if (SaleType.CREDIT.equals(s.getType())) creditSales = creditSales.add(amt);
            else if (SaleType.UPI.equals(s.getType()))    upiSales    = upiSales.add(amt);
            else if (SaleType.CARD.equals(s.getType()))   cardSales   = cardSales.add(amt);
        }

        List<SaleResponse> saleResponses = sales.stream().map(saleMapper::toResponse).toList();

        return ReportResponse.builder()
                .reportType(type)
                .date(date)
                .year(year)
                .month(month)
                .totalSales(totalSales)
                .cashSales(cashSales)
                .creditSales(creditSales)
                .upiSales(upiSales)
                .cardSales(cardSales)
                .totalPaid(totalPaid)
                .totalBalance(totalBalance)
                .saleCount((long) sales.size())
                .sales(saleResponses)
                .build();
    }

    private BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
