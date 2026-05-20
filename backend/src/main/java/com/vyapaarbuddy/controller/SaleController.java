package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.SaleRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.SaleResponse;
import com.vyapaarbuddy.dto.response.SalesSummaryResponse;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Sales entry and reporting APIs")
@SecurityRequirement(name = "bearerAuth")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Create sale",
               description = "Records a new sale. For CREDIT sales or when balanceAmount > 0, customerId is required.")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody SaleRequest request) {
        SaleResponse response = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Sale created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<ApiResponse<SaleResponse>> getSale(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(saleService.getSaleById(id)));
    }

    @GetMapping
    @Operation(summary = "List sales",
               description = "List sales for logged-in business. Filter by fromDate+toDate, customerId, or saleType.")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> listSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) SaleType saleType) {
        return ResponseEntity.ok(ApiResponse.ok(
                saleService.listSales(fromDate, toDate, customerId, saleType)));
    }

    @GetMapping("/summary/daily")
    @Operation(summary = "Daily sales summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponse>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(saleService.getDailySummary(date)));
    }

    @GetMapping("/summary/monthly")
    @Operation(summary = "Monthly sales summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponse>> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(saleService.getMonthlySummary(year, month)));
    }
}
