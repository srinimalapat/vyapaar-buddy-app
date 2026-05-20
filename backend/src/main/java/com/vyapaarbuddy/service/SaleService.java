package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.SaleRequest;
import com.vyapaarbuddy.dto.response.SaleResponse;
import com.vyapaarbuddy.dto.response.SalesSummaryResponse;
import com.vyapaarbuddy.enums.SaleType;

import java.time.LocalDate;
import java.util.List;

public interface SaleService {

    SaleResponse createSale(SaleRequest request);

    SaleResponse getSaleById(Long id);

    List<SaleResponse> listSales(LocalDate fromDate, LocalDate toDate, Long customerId, SaleType saleType);

    SalesSummaryResponse getDailySummary(LocalDate date);

    SalesSummaryResponse getMonthlySummary(int year, int month);
}
