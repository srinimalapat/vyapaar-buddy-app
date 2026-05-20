package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.DashboardResponse;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard stats API")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Get dashboard stats for the logged-in business")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardStats() {
        Long businessId = currentUserService.getCurrentBusinessId();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboardStats(businessId)));
    }
}
