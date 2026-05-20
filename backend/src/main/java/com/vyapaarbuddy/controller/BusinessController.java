package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.BusinessRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.BusinessResponse;
import com.vyapaarbuddy.service.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
@Tag(name = "Business", description = "Business profile management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping("/me")
    @Operation(summary = "Get my business profile",
               description = "Returns the business profile of the currently authenticated user")
    public ResponseEntity<ApiResponse<BusinessResponse>> getMyBusiness() {
        return ResponseEntity.ok(ApiResponse.ok(businessService.getMyBusiness()));
    }

    @PostMapping
    @Operation(summary = "Create business profile",
               description = "Creates a new business profile for the currently authenticated user. Each user can have only one business profile.")
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(
            @Valid @RequestBody BusinessRequest request) {
        BusinessResponse response = businessService.createBusiness(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Business profile created successfully", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my business profile",
               description = "Updates the business profile of the currently authenticated user")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateMyBusiness(
            @Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Business profile updated successfully",
                businessService.updateMyBusiness(request)));
    }
}
