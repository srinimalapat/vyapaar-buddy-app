package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.SaleItemRequest;
import com.vyapaarbuddy.dto.request.SaleRequest;
import com.vyapaarbuddy.dto.response.SaleResponse;
import com.vyapaarbuddy.entity.*;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.mapper.SaleMapper;
import com.vyapaarbuddy.repository.*;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.SaleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private SaleItemRepository saleItemRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CreditTransactionRepository creditTransactionRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Business business;
    private Customer customer;

    @BeforeEach
    void setUp() {
        business = Business.builder().id(1L).name("Test Shop").build();
        customer = Customer.builder()
                .id(10L).name("Ramesh").status(CustomerStatus.ACTIVE)
                .creditBalance(BigDecimal.ZERO).business(business).build();
    }

    private SaleRequest buildRequest(SaleType type, BigDecimal total, BigDecimal paid, Long customerId) {
        SaleRequest req = new SaleRequest();
        req.setSaleType(type);
        req.setTotalAmount(total);
        req.setPaidAmount(paid);
        req.setCustomerId(customerId);
        SaleItemRequest item = new SaleItemRequest();
        item.setItemName("Rice");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        req.setItems(List.of(item));
        return req;
    }

    @Test
    void createCashSale_success_noCustomerRequired() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.empty());
        Sale saved = Sale.builder().id(1L).type(SaleType.CASH)
                .totalAmount(BigDecimal.valueOf(200)).paidAmount(BigDecimal.valueOf(200)).build();
        when(saleRepository.save(any())).thenReturn(saved);
        when(saleItemRepository.saveAll(any())).thenReturn(List.of());
        when(saleMapper.toResponse(saved)).thenReturn(SaleResponse.builder().id(1L).build());

        SaleResponse response = saleService.createSale(buildRequest(SaleType.CASH,
                BigDecimal.valueOf(200), BigDecimal.valueOf(200), null));

        assertNotNull(response);
        verify(customerRepository, never()).save(any());
        verify(creditTransactionRepository, never()).save(any());
    }

    @Test
    void createCreditSale_updatesCustomerBalanceAndCreatesCreditTransaction() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(customerRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(customer));
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.empty());
        Sale saved = Sale.builder().id(2L).type(SaleType.CREDIT)
                .totalAmount(BigDecimal.valueOf(500)).paidAmount(BigDecimal.valueOf(200))
                .saleDate(LocalDate.now()).customer(customer).build();
        when(saleRepository.save(any())).thenReturn(saved);
        when(saleItemRepository.saveAll(any())).thenReturn(List.of());
        when(saleMapper.toResponse(saved)).thenReturn(SaleResponse.builder().id(2L).build());

        saleService.createSale(buildRequest(SaleType.CREDIT,
                BigDecimal.valueOf(500), BigDecimal.valueOf(200), 10L));

        verify(customerRepository).save(customer);
        verify(creditTransactionRepository).save(any());
        assertEquals(new BigDecimal("300.00"), customer.getCreditBalance());
    }

    @Test
    void createSale_paidExceedsTotal_throwsBadRequest() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                saleService.createSale(buildRequest(SaleType.CASH,
                        BigDecimal.valueOf(100), BigDecimal.valueOf(200), null)));
    }

    @Test
    void createCreditSale_noCustomer_throwsBadRequest() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                saleService.createSale(buildRequest(SaleType.CREDIT,
                        BigDecimal.valueOf(500), BigDecimal.valueOf(200), null)));
    }

    @Test
    void itemTotalPrice_calculatedCorrectly() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.empty());
        Sale saved = Sale.builder().id(3L).type(SaleType.CASH)
                .totalAmount(BigDecimal.valueOf(200)).paidAmount(BigDecimal.valueOf(200)).build();
        when(saleRepository.save(any())).thenReturn(saved);
        when(saleItemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(saleMapper.toResponse(any())).thenReturn(SaleResponse.builder().id(3L).build());

        saleService.createSale(buildRequest(SaleType.CASH,
                BigDecimal.valueOf(200), BigDecimal.valueOf(200), null));

        verify(saleItemRepository).saveAll(argThat(items -> {
            @SuppressWarnings("unchecked")
            SaleItem item = ((List<SaleItem>) items).get(0);
            return item.getTotalPrice().compareTo(new BigDecimal("200.00")) == 0;
        }));
    }

    @Test
    void createSale_reducesInventoryStock_whenItemTracked() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        InventoryItem invItem = InventoryItem.builder()
                .id(5L).name("Rice").quantity(10).status(InventoryStatus.ACTIVE)
                .business(business).build();
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.of(invItem));
        when(inventoryItemRepository.save(invItem)).thenReturn(invItem);
        Sale saved = Sale.builder().id(4L).type(SaleType.CASH)
                .totalAmount(BigDecimal.valueOf(200)).paidAmount(BigDecimal.valueOf(200)).build();
        when(saleRepository.save(any())).thenReturn(saved);
        when(saleItemRepository.saveAll(any())).thenReturn(List.of());
        when(saleMapper.toResponse(any())).thenReturn(SaleResponse.builder().id(4L).build());

        saleService.createSale(buildRequest(SaleType.CASH,
                BigDecimal.valueOf(200), BigDecimal.valueOf(200), null));

        assertEquals(8, invItem.getQuantity()); // 10 - 2
        verify(inventoryItemRepository).save(invItem);
    }

    @Test
    void createSale_insufficientStock_throwsBadRequest() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        InventoryItem invItem = InventoryItem.builder()
                .id(5L).name("Rice").quantity(1).status(InventoryStatus.ACTIVE)
                .business(business).build();
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.of(invItem));

        assertThrows(BadRequestException.class, () ->
                saleService.createSale(buildRequest(SaleType.CASH,
                        BigDecimal.valueOf(200), BigDecimal.valueOf(200), null)));
    }

    @Test
    void createSale_itemNotInInventory_saleAllowed() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(
                eq(1L), eq("Rice"), eq(InventoryStatus.ACTIVE))).thenReturn(Optional.empty());
        Sale saved = Sale.builder().id(5L).type(SaleType.CASH)
                .totalAmount(BigDecimal.valueOf(200)).paidAmount(BigDecimal.valueOf(200)).build();
        when(saleRepository.save(any())).thenReturn(saved);
        when(saleItemRepository.saveAll(any())).thenReturn(List.of());
        when(saleMapper.toResponse(any())).thenReturn(SaleResponse.builder().id(5L).build());

        SaleResponse response = saleService.createSale(buildRequest(SaleType.CASH,
                BigDecimal.valueOf(200), BigDecimal.valueOf(200), null));

        assertNotNull(response);
        verify(inventoryItemRepository, never()).save(any());
    }
}
