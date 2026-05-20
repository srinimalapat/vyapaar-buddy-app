package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.response.DashboardResponse;
import com.vyapaarbuddy.entity.Sale;
import com.vyapaarbuddy.enums.CreditTransactionType;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.mapper.CreditMapper;
import com.vyapaarbuddy.mapper.SaleMapper;
import com.vyapaarbuddy.repository.CreditTransactionRepository;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.repository.SaleRepository;
import com.vyapaarbuddy.service.impl.DashboardServiceImpl;
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
class DashboardServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CreditTransactionRepository creditTransactionRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private CreditMapper creditMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboardStats_returnsTodaySales() {
        Long businessId = 1L;
        LocalDate today = LocalDate.now();

        Sale cashSale = Sale.builder().id(1L).type(SaleType.CASH)
                .totalAmount(BigDecimal.valueOf(500)).paidAmount(BigDecimal.valueOf(500))
                .saleDate(today).build();
        Sale creditSale = Sale.builder().id(2L).type(SaleType.CREDIT)
                .totalAmount(BigDecimal.valueOf(300)).paidAmount(BigDecimal.ZERO)
                .saleDate(today).build();

        when(saleRepository.findByBusinessIdAndSaleDate(businessId, today))
                .thenReturn(List.of(cashSale, creditSale));
        when(saleRepository.sumTotalByBusinessIdAndDateRange(eq(businessId), any(), any()))
                .thenReturn(BigDecimal.valueOf(800));
        when(customerRepository.sumOutstandingCreditByBusinessId(businessId))
                .thenReturn(BigDecimal.valueOf(300));
        when(customerRepository.countCustomersWithOutstandingCredit(businessId)).thenReturn(1L);
        when(inventoryItemRepository.findActiveLowStockItems(businessId)).thenReturn(List.of());
        when(saleRepository.findTop5ByBusinessIdOrderByCreatedAtDesc(businessId)).thenReturn(List.of());
        when(creditTransactionRepository.findTop5ByBusinessIdAndTypeOrderByCreatedAtDesc(
                businessId, CreditTransactionType.PAYMENT_RECEIVED)).thenReturn(List.of());

        DashboardResponse stats = dashboardService.getDashboardStats(businessId);

        assertNotNull(stats);
        assertEquals(0, BigDecimal.valueOf(800).compareTo(stats.getTodayTotalSales()));
        assertEquals(0, BigDecimal.valueOf(500).compareTo(stats.getTodayCashSales()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(stats.getTodayCreditSales()));
    }

    @Test
    void getDashboardStats_returnsPendingUdhaar() {
        Long businessId = 1L;

        when(saleRepository.findByBusinessIdAndSaleDate(eq(businessId), any())).thenReturn(List.of());
        when(saleRepository.sumTotalByBusinessIdAndDateRange(eq(businessId), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(customerRepository.sumOutstandingCreditByBusinessId(businessId))
                .thenReturn(BigDecimal.valueOf(1500));
        when(customerRepository.countCustomersWithOutstandingCredit(businessId)).thenReturn(3L);
        when(inventoryItemRepository.findActiveLowStockItems(businessId)).thenReturn(List.of());
        when(saleRepository.findTop5ByBusinessIdOrderByCreatedAtDesc(businessId)).thenReturn(List.of());
        when(creditTransactionRepository.findTop5ByBusinessIdAndTypeOrderByCreatedAtDesc(
                businessId, CreditTransactionType.PAYMENT_RECEIVED)).thenReturn(List.of());

        DashboardResponse stats = dashboardService.getDashboardStats(businessId);

        assertEquals(0, BigDecimal.valueOf(1500).compareTo(stats.getTotalPendingUdhaar()));
        assertEquals(3L, stats.getCustomersWithPendingCredit());
    }

    @Test
    void getDashboardStats_returnsLowStockCount() {
        Long businessId = 1L;

        when(saleRepository.findByBusinessIdAndSaleDate(eq(businessId), any())).thenReturn(List.of());
        when(saleRepository.sumTotalByBusinessIdAndDateRange(eq(businessId), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(customerRepository.sumOutstandingCreditByBusinessId(businessId)).thenReturn(BigDecimal.ZERO);
        when(customerRepository.countCustomersWithOutstandingCredit(businessId)).thenReturn(0L);
        when(inventoryItemRepository.findActiveLowStockItems(businessId))
                .thenReturn(List.of(
                        com.vyapaarbuddy.entity.InventoryItem.builder().id(1L).build(),
                        com.vyapaarbuddy.entity.InventoryItem.builder().id(2L).build()));
        when(saleRepository.findTop5ByBusinessIdOrderByCreatedAtDesc(businessId)).thenReturn(List.of());
        when(creditTransactionRepository.findTop5ByBusinessIdAndTypeOrderByCreatedAtDesc(
                businessId, CreditTransactionType.PAYMENT_RECEIVED)).thenReturn(List.of());

        DashboardResponse stats = dashboardService.getDashboardStats(businessId);

        assertEquals(2L, stats.getLowStockCount());
    }

    @Test
    void getDashboardStats_returnsRecentSales() {
        Long businessId = 1L;
        Sale s = Sale.builder().id(10L).type(SaleType.CASH)
                .totalAmount(BigDecimal.valueOf(100)).paidAmount(BigDecimal.valueOf(100)).build();

        when(saleRepository.findByBusinessIdAndSaleDate(eq(businessId), any())).thenReturn(List.of());
        when(saleRepository.sumTotalByBusinessIdAndDateRange(eq(businessId), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(customerRepository.sumOutstandingCreditByBusinessId(businessId)).thenReturn(BigDecimal.ZERO);
        when(customerRepository.countCustomersWithOutstandingCredit(businessId)).thenReturn(0L);
        when(inventoryItemRepository.findActiveLowStockItems(businessId)).thenReturn(List.of());
        when(saleRepository.findTop5ByBusinessIdOrderByCreatedAtDesc(businessId)).thenReturn(List.of(s));
        when(creditTransactionRepository.findTop5ByBusinessIdAndTypeOrderByCreatedAtDesc(
                businessId, CreditTransactionType.PAYMENT_RECEIVED)).thenReturn(List.of());
        when(saleMapper.toResponse(s)).thenReturn(
                com.vyapaarbuddy.dto.response.SaleResponse.builder().id(10L).build());

        DashboardResponse stats = dashboardService.getDashboardStats(businessId);

        assertEquals(1, stats.getRecentSales().size());
        assertEquals(10L, stats.getRecentSales().get(0).getId());
    }
}
