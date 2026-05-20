package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.CreditTransactionRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.CreditTransactionResponse;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.dto.response.TotalOutstandingCreditResponse;
import com.vyapaarbuddy.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
@Tag(name = "Credits / Udhaar", description = "Credit and udhaar tracking APIs")
@SecurityRequirement(name = "bearerAuth")
public class CreditController {

    private final CreditService creditService;

    @PostMapping
    @Operation(summary = "Add credit transaction",
               description = "Manually add a credit transaction (CREDIT_GIVEN, PAYMENT_RECEIVED, or ADJUSTMENT)")
    public ResponseEntity<ApiResponse<CreditTransactionResponse>> addCreditTransaction(
            @Valid @RequestBody CreditTransactionRequest request) {
        CreditTransactionResponse response = creditService.addCreditTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Credit transaction recorded", response));
    }

    @PostMapping("/payments")
    @Operation(summary = "Record payment received",
               description = "Record a payment received from a customer. Reduces their outstanding balance.")
    public ResponseEntity<ApiResponse<CreditTransactionResponse>> recordPayment(
            @Valid @RequestBody CreditTransactionRequest request) {
        CreditTransactionResponse response = creditService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Payment recorded successfully", response));
    }

    @GetMapping("/customers/{customerId}/history")
    @Operation(summary = "Customer credit history",
               description = "Returns all credit transactions for a customer ordered by date desc")
    public ResponseEntity<ApiResponse<List<CreditTransactionResponse>>> getCreditHistory(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                creditService.getCustomerCreditHistory(customerId)));
    }

    @GetMapping("/pending-customers")
    @Operation(summary = "Customers with pending credit",
               description = "Returns all active customers who have an outstanding credit balance")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getPendingCreditCustomers() {
        return ResponseEntity.ok(ApiResponse.ok(creditService.getPendingCreditCustomers()));
    }

    @GetMapping("/total-outstanding")
    @Operation(summary = "Total outstanding credit",
               description = "Returns aggregate outstanding credit and count of customers with pending balance")
    public ResponseEntity<ApiResponse<TotalOutstandingCreditResponse>> getTotalOutstanding() {
        return ResponseEntity.ok(ApiResponse.ok(creditService.getTotalOutstanding()));
    }
}
