package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.SaleItemRequest;
import com.vyapaarbuddy.dto.request.SaleRequest;
import com.vyapaarbuddy.dto.response.SaleResponse;
import com.vyapaarbuddy.dto.response.SalesSummaryResponse;
import com.vyapaarbuddy.entity.*;
import com.vyapaarbuddy.enums.CreditTransactionType;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.SaleMapper;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.repository.*;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.SaleService;
import com.vyapaarbuddy.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final CustomerRepository customerRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SaleMapper saleMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        Business business = currentUserService.getCurrentBusiness();

        // Resolve and validate customer
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByBusinessIdAndId(business.getId(), request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found or does not belong to your business"));
            if (CustomerStatus.INACTIVE.equals(customer.getStatus())) {
                throw new BadRequestException("Cannot create sale for inactive customer");
            }
        }

        // Build sale items and compute totals
        List<SaleItem> saleItems = buildSaleItems(request.getItems());

        // Reduce inventory stock for tracked items (best-effort: skip if item not in inventory)
        reduceInventoryStock(business.getId(), request.getItems());

        BigDecimal computedTotal = saleItems.stream()
                .map(SaleItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = (MoneyUtil.isPositive(request.getTotalAmount()))
                ? request.getTotalAmount() : computedTotal;
        BigDecimal paidAmount = MoneyUtil.defaultZero(request.getPaidAmount());
        BigDecimal balanceAmount = MoneyUtil.calculateBalance(totalAmount, paidAmount);

        // Validations
        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new BadRequestException("Paid amount cannot be greater than total amount");
        }

        boolean isCreditSale = SaleType.CREDIT.equals(request.getSaleType())
                || MoneyUtil.isPositive(balanceAmount);

        if (isCreditSale && customer == null) {
            throw new BadRequestException("Customer is required for credit sale");
        }

        // Persist sale
        Sale sale = Sale.builder()
                .business(business)
                .customer(customer)
                .saleDate(request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now())
                .type(request.getSaleType())
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .notes(request.getNotes())
                .build();

        Sale saved = saleRepository.save(sale);

        // Persist sale items linked to saved sale
        for (SaleItem item : saleItems) {
            item.setSale(saved);
        }
        List<SaleItem> persistedItems = saleItemRepository.saveAll(saleItems);
        saved.getItems().addAll(persistedItems);

        // Handle credit: update customer balance + create credit transaction
        if (isCreditSale && MoneyUtil.isPositive(balanceAmount)) {
            customer.setCreditBalance(MoneyUtil.add(
                    MoneyUtil.defaultZero(customer.getCreditBalance()), balanceAmount));
            customerRepository.save(customer);

            CreditTransaction creditTx = CreditTransaction.builder()
                    .business(business)
                    .customer(customer)
                    .type(CreditTransactionType.CREDIT_GIVEN)
                    .amount(balanceAmount)
                    .transactionDate(saved.getSaleDate())
                    .description("Credit from Sale #" + saved.getId())
                    .build();
            creditTransactionRepository.save(creditTx);
        }

        return saleMapper.toResponse(saved);
    }

    @Override
    public SaleResponse getSaleById(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Sale sale = saleRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        return saleMapper.toResponse(sale);
    }

    @Override
    public List<SaleResponse> listSales(LocalDate fromDate, LocalDate toDate,
                                        Long customerId, SaleType saleType) {
        Long businessId = currentUserService.getCurrentBusinessId();
        List<Sale> sales;

        if (fromDate != null && toDate != null) {
            sales = saleRepository.findByBusinessIdAndSaleDateBetweenOrderBySaleDateDescCreatedAtDesc(
                    businessId, fromDate, toDate);
        } else if (customerId != null) {
            sales = saleRepository.findByBusinessIdAndCustomerIdOrderBySaleDateDescCreatedAtDesc(
                    businessId, customerId);
        } else if (saleType != null) {
            sales = saleRepository.findByBusinessIdAndTypeOrderBySaleDateDescCreatedAtDesc(
                    businessId, saleType);
        } else {
            sales = saleRepository.findByBusinessIdOrderBySaleDateDescCreatedAtDesc(businessId);
        }

        return sales.stream().map(saleMapper::toResponse).toList();
    }

    @Override
    public SalesSummaryResponse getDailySummary(LocalDate date) {
        Long businessId = currentUserService.getCurrentBusinessId();
        List<Sale> sales = saleRepository.findByBusinessIdAndSaleDate(businessId, date);
        return buildSummary(sales, date, null, null);
    }

    @Override
    public SalesSummaryResponse getMonthlySummary(int year, int month) {
        Long businessId = currentUserService.getCurrentBusinessId();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        List<Sale> sales = saleRepository.findByBusinessIdAndSaleDateBetween(businessId, from, to);
        return buildSummary(sales, null, year, month);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private List<SaleItem> buildSaleItems(List<SaleItemRequest> requests) {
        List<SaleItem> items = new ArrayList<>();
        for (SaleItemRequest req : requests) {
            BigDecimal totalPrice = MoneyUtil.multiply(req.getUnitPrice(),
                    BigDecimal.valueOf(req.getQuantity()));
            items.add(SaleItem.builder()
                    .itemName(req.getItemName())
                    .quantity(req.getQuantity())
                    .unitPrice(req.getUnitPrice())
                    .totalPrice(totalPrice)
                    .build());
        }
        return items;
    }

    private void reduceInventoryStock(Long businessId, List<SaleItemRequest> items) {
        for (SaleItemRequest req : items) {
            inventoryItemRepository
                    .findByBusinessIdAndNameIgnoreCaseAndStatus(businessId, req.getItemName(), InventoryStatus.ACTIVE)
                    .ifPresent(invItem -> {
                        int available = invItem.getQuantity() != null ? invItem.getQuantity() : 0;
                        if (available < req.getQuantity()) {
                            throw new BadRequestException(
                                    "Insufficient stock for item: " + req.getItemName()
                                    + " (available: " + available + ", requested: " + req.getQuantity() + ")");
                        }
                        invItem.setQuantity(available - req.getQuantity());
                        inventoryItemRepository.save(invItem);
                    });
        }
    }

    private SalesSummaryResponse buildSummary(List<Sale> sales, LocalDate date, Integer year, Integer month) {
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal cashSales = BigDecimal.ZERO;
        BigDecimal creditSales = BigDecimal.ZERO;
        BigDecimal upiSales = BigDecimal.ZERO;
        BigDecimal cardSales = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Sale s : sales) {
            BigDecimal total = MoneyUtil.defaultZero(s.getTotalAmount());
            BigDecimal paid = MoneyUtil.defaultZero(s.getPaidAmount());
            totalSales = MoneyUtil.add(totalSales, total);
            totalPaid = MoneyUtil.add(totalPaid, paid);
            totalBalance = MoneyUtil.add(totalBalance, MoneyUtil.calculateBalance(total, paid));
            if (SaleType.CASH.equals(s.getType())) cashSales = MoneyUtil.add(cashSales, total);
            else if (SaleType.CREDIT.equals(s.getType())) creditSales = MoneyUtil.add(creditSales, total);
            else if (SaleType.UPI.equals(s.getType())) upiSales = MoneyUtil.add(upiSales, total);
            else if (SaleType.CARD.equals(s.getType())) cardSales = MoneyUtil.add(cardSales, total);
        }

        return SalesSummaryResponse.builder()
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
                .saleCount(sales.size())
                .build();
    }
}
