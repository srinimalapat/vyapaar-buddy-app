package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.CancelFileStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmFileStockEntryRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.FileStockEntryResponse;
import com.vyapaarbuddy.enums.FileStockEntryStatus;
import com.vyapaarbuddy.service.FileStockEntryService;
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
@RequestMapping("/api/v1/file-stock")
@RequiredArgsConstructor
@Tag(name = "File Stock Entry", description = "Upload images or documents (PDF, Excel, CSV, Word, TXT) to extract and confirm inventory items")
@SecurityRequirement(name = "bearerAuth")
public class FileStockEntryController {

    private final FileStockEntryService fileStockEntryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload file and extract stock items",
        description = """
            Supported file types: JPG, PNG, WEBP, PDF, XLS/XLSX, CSV, DOCX, TXT.
            For image files or scanned PDFs, provide 'mockText' for local MVP testing.
            TXT, CSV, Excel, and digital-text PDFs are extracted automatically.
            Inventory is NOT updated at this step — confirm separately.

            Example mockText:
              Rice 25kg 60
              Sugar 10kg 45
              Oil 5L 140
            """
    )
    public ResponseEntity<ApiResponse<FileStockEntryResponse>> uploadAndExtract(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "mockText", required = false) String mockText) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("File uploaded and text extracted",
                        fileStockEntryService.uploadAndExtract(file, mockText)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file stock entry by ID")
    public ResponseEntity<ApiResponse<FileStockEntryResponse>> getEntry(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(fileStockEntryService.getEntry(id)));
    }

    @GetMapping
    @Operation(summary = "List file stock entries", description = "Optional ?status= filter: PENDING_REVIEW | CONFIRMED | CANCELLED | FAILED")
    public ResponseEntity<ApiResponse<List<FileStockEntryResponse>>> listEntries(
            @RequestParam(required = false) FileStockEntryStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(fileStockEntryService.listEntries(status)));
    }

    @PostMapping("/{id}/confirm")
    @Operation(
        summary = "Confirm items and update inventory",
        description = "Pass edited items to override extracted values. If omitted, extracted items are used. " +
                      "Inventory is only updated at this step — never at upload time."
    )
    public ResponseEntity<ApiResponse<FileStockEntryResponse>> confirmEntry(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmFileStockEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory updated successfully",
                fileStockEntryService.confirmEntry(id, request)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending file stock entry")
    public ResponseEntity<ApiResponse<FileStockEntryResponse>> cancelEntry(
            @PathVariable Long id,
            @RequestBody(required = false) CancelFileStockEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("File stock entry cancelled",
                fileStockEntryService.cancelEntry(id, request)));
    }
}
