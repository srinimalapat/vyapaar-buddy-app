package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.InventoryItemRequest;
import com.vyapaarbuddy.dto.request.StockUpdateRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.InventoryItemResponse;
import com.vyapaarbuddy.service.InventoryService;
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
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management APIs")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Add inventory item")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> addItem(
            @Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory item added", inventoryService.addItem(request)));
    }

    @GetMapping
    @Operation(summary = "List inventory items",
               description = "Optional ?search= and ?status= filters")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> listItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.listItems(search, status)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get active items where quantity <= lowStockThreshold")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getLowStockItems() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStockItems()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory item by ID")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getItemById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inventory item")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory item updated", inventoryService.updateItem(id, request)));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Manually update stock quantity")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Stock updated",
                inventoryService.updateStock(id, request.getQuantityAvailable())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate inventory item (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deactivateItem(@PathVariable Long id) {
        inventoryService.deactivateItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Inventory item deactivated", null));
    }
}
