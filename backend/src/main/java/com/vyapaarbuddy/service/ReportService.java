package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.response.ReportResponse;

import java.time.LocalDate;

public interface ReportService {

    ReportResponse getDailySalesReport(LocalDate date);

    ReportResponse getMonthlySalesReport(int year, int month);

    ReportResponse getCustomerCreditReport();

    ReportResponse getInventoryLowStockReport();
}
