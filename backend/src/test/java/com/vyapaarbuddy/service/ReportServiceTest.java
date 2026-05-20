package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.response.ReportResponse;
import com.vyapaarbuddy.entity.Customer;
import com.vyapaarbuddy.entity.InventoryItem;
import com.vyapaarbuddy.entity.Sale;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.mapper.CustomerMapper;
import com.vyapaarbuddy.mapper.InventoryMapper;
import com.vyapaarbuddy.mapper.SaleMapper;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.repository.SaleRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private CustomerMapper customerMapper;
    @Mock private InventoryMapper inventoryMapper;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
    }

    @Test
    void getDailySalesReport_returnsSalesForDate() {
        LocalDate date = LocalDate.of(2026, 5, 20);
        Sale s = Sale.builder().id(1L).type(SaleType.CASH)
                .totalAmount(BigDecimal.valueOf(500)).paidAmount(BigDecimal.valueOf(500))
                .saleDate(date).build();
        when(saleRepository.findByBusinessIdAndSaleDate(1L, date)).thenReturn(List.of(s));
        when(saleMapper.toResponse(s)).thenReturn(
                com.vyapaarbuddy.dto.response.SaleResponse.builder().id(1L).build());

        ReportResponse report = reportService.getDailySalesReport(date);

        assertEquals("DAILY_SALES", report.getReportType());
        assertEquals(date, report.getDate());
        assertEquals(1L, report.getSaleCount());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(report.getTotalSales()));
        assertEquals(0, BigDecimal.valueOf(500).compareTo(report.getCashSales()));
    }

    @Test
    void getDailySalesReport_defaultsToToday_whenDateNull() {
        LocalDate today = LocalDate.now();
        when(saleRepository.findByBusinessIdAndSaleDate(1L, today)).thenReturn(List.of());

        ReportResponse report = reportService.getDailySalesReport(null);

        assertEquals(today, report.getDate());
    }

    @Test
    void getMonthlySalesReport_returnsMonthlyData() {
        Sale s = Sale.builder().id(1L).type(SaleType.CREDIT)
                .totalAmount(BigDecimal.valueOf(800)).paidAmount(BigDecimal.ZERO).build();
        when(saleRepository.findByBusinessIdAndSaleDateBetween(eq(1L), any(), any()))
                .thenReturn(List.of(s));
        when(saleMapper.toResponse(s)).thenReturn(
                com.vyapaarbuddy.dto.response.SaleResponse.builder().id(1L).build());

        ReportResponse report = reportService.getMonthlySalesReport(2026, 5);

        assertEquals("MONTHLY_SALES", report.getReportType());
        assertEquals(2026, report.getYear());
        assertEquals(5, report.getMonth());
        assertEquals(0, BigDecimal.valueOf(800).compareTo(report.getCreditSales()));
    }

    @Test
    void getMonthlySalesReport_invalidMonth_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> reportService.getMonthlySalesReport(2026, 13));
        assertThrows(BadRequestException.class, () -> reportService.getMonthlySalesReport(2026, 0));
    }

    @Test
    void getCustomerCreditReport_returnsActiveDebtors() {
        Customer c = Customer.builder().id(1L).name("Ramesh")
                .status(CustomerStatus.ACTIVE).creditBalance(BigDecimal.valueOf(500)).build();
        when(customerRepository.findCustomersWithOutstandingCredit(1L)).thenReturn(List.of(c));
        when(customerRepository.sumOutstandingCreditByBusinessId(1L)).thenReturn(BigDecimal.valueOf(500));
        when(customerRepository.countCustomersWithOutstandingCredit(1L)).thenReturn(1L);
        when(customerMapper.toResponse(c)).thenReturn(
                com.vyapaarbuddy.dto.response.CustomerResponse.builder()
                        .id(1L).customerName("Ramesh")
                        .totalCreditAmount(BigDecimal.valueOf(500)).build());

        ReportResponse report = reportService.getCustomerCreditReport();

        assertEquals("CUSTOMER_CREDIT", report.getReportType());
        assertEquals(1, report.getCustomers().size());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(report.getTotalOutstandingCredit()));
    }

    @Test
    void getInventoryLowStockReport_returnsLowStockItems() {
        InventoryItem item = InventoryItem.builder().id(1L).name("Sugar")
                .quantity(3).lowStockThreshold(10).status(InventoryStatus.ACTIVE).build();
        when(inventoryItemRepository.findActiveLowStockItems(1L)).thenReturn(List.of(item));
        when(inventoryMapper.toResponse(item)).thenReturn(
                com.vyapaarbuddy.dto.response.InventoryItemResponse.builder()
                        .id(1L).itemName("Sugar").quantityAvailable(3).lowStock(true).build());

        ReportResponse report = reportService.getInventoryLowStockReport();

        assertEquals("INVENTORY_LOW_STOCK", report.getReportType());
        assertEquals(1L, report.getLowStockCount());
        assertEquals(1, report.getInventoryItems().size());
    }
}
