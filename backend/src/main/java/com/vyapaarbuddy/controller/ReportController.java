package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.ReportResponse;
import com.vyapaarbuddy.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Business report APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily-sales")
    @Operation(summary = "Daily sales report",
               description = "date defaults to today if omitted")
    public ResponseEntity<ApiResponse<ReportResponse>> getDailySalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getDailySalesReport(date)));
    }

    @GetMapping("/monthly-sales")
    @Operation(summary = "Monthly sales report")
    public ResponseEntity<ApiResponse<ReportResponse>> getMonthlySalesReport(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getMonthlySalesReport(year, month)));
    }

    @GetMapping("/customer-credit")
    @Operation(summary = "Customer credit (udhaar) report")
    public ResponseEntity<ApiResponse<ReportResponse>> getCustomerCreditReport() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getCustomerCreditReport()));
    }

    @GetMapping("/inventory-low-stock")
    @Operation(summary = "Inventory low-stock report")
    public ResponseEntity<ApiResponse<ReportResponse>> getInventoryLowStockReport() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getInventoryLowStockReport()));
    }
}
