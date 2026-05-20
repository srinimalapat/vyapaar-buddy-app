package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.CustomerRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create customer",
               description = "Creates a new customer for the logged-in user's business")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID",
               description = "Returns a customer only if it belongs to the logged-in user's business")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getCustomerById(id)));
    }

    @GetMapping
    @Operation(summary = "List all customers",
               description = "Returns all customers for the logged-in user's business. Defaults to ACTIVE status if no filter given.")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getAllCustomers(status)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers",
               description = "Search customers by name or mobile number within the logged-in user's business")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> searchCustomers(
            @RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.searchCustomers(query)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer",
               description = "Updates a customer only if it belongs to the logged-in user's business")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully",
                customerService.updateCustomer(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate customer",
               description = "Soft-deletes a customer by setting status to INACTIVE. The record is not removed from the database.")
    public ResponseEntity<ApiResponse<Void>> deactivateCustomer(@PathVariable Long id) {
        customerService.deactivateCustomer(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer deactivated successfully", null));
    }
}
