package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.CancelPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.PhotoStockEntryResponse;
import com.vyapaarbuddy.enums.PhotoStockEntryStatus;
import com.vyapaarbuddy.service.PhotoStockEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/photo-stock")
@RequiredArgsConstructor
@Tag(name = "Photo Stock Entry", description = "Upload item list photos and extract inventory data")
@SecurityRequirement(name = "bearerAuth")
public class PhotoStockEntryController {

    private final PhotoStockEntryService photoStockEntryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload image and extract stock items",
        description = "Upload a supplier bill, notebook page, or stock sheet image. " +
                      "For local testing, provide 'mockText' instead of running OCR. " +
                      "Example mockText:\\nRice 25kg 60\\nSugar 10kg 45\\nOil 5L 140"
    )
    public ResponseEntity<ApiResponse<PhotoStockEntryResponse>> uploadAndExtract(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "mockText", required = false) String mockText) {
        PhotoStockEntryResponse response = photoStockEntryService.uploadAndExtract(file, mockText);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Image uploaded and text extracted", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get photo stock entry by ID")
    public ResponseEntity<ApiResponse<PhotoStockEntryResponse>> getEntry(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(photoStockEntryService.getEntry(id)));
    }

    @GetMapping
    @Operation(summary = "List photo stock entries", description = "Optional ?status= filter (PENDING_REVIEW, CONFIRMED, CANCELLED, FAILED)")
    public ResponseEntity<ApiResponse<List<PhotoStockEntryResponse>>> listEntries(
            @RequestParam(required = false) PhotoStockEntryStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(photoStockEntryService.listEntries(status)));
    }

    @PostMapping("/{id}/confirm")
    @Operation(
        summary = "Confirm extracted items and update inventory",
        description = "Pass edited items to override extracted values. " +
                      "If no items provided, original extracted items are used. " +
                      "Inventory is only updated here — never at upload time."
    )
    public ResponseEntity<ApiResponse<PhotoStockEntryResponse>> confirmEntry(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmPhotoStockEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory updated successfully",
                photoStockEntryService.confirmEntry(id, request)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending photo stock entry")
    public ResponseEntity<ApiResponse<PhotoStockEntryResponse>> cancelEntry(
            @PathVariable Long id,
            @RequestBody(required = false) CancelPhotoStockEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Photo stock entry cancelled",
                photoStockEntryService.cancelEntry(id, request)));
    }
}
