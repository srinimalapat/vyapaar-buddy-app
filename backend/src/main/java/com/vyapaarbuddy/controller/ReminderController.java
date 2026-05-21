package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.ReminderRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.ReminderResponse;
import com.vyapaarbuddy.dto.response.ReminderSendResponse;
import com.vyapaarbuddy.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
@Tag(name = "Reminders", description = "Payment reminder APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/customer/{customerId}")
    @Operation(summary = "Generate reminder for a customer",
               description = "Request body is optional. Defaults: amountDue=creditBalance, channel=WHATSAPP_MANUAL, message=auto-generated Hindi")
    public ResponseEntity<ApiResponse<ReminderResponse>> generateReminder(
            @PathVariable Long customerId,
            @RequestBody(required = false) ReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Reminder generated", reminderService.generateReminder(customerId, request)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk generate reminders for all customers with outstanding credit")
    public ResponseEntity<ApiResponse<List<ReminderResponse>>> bulkGenerate() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bulk reminders generated", reminderService.bulkGenerate()));
    }

    @GetMapping
    @Operation(summary = "List reminders",
               description = "Optional ?customerId= and ?status= filters")
    public ResponseEntity<ApiResponse<List<ReminderResponse>>> listReminders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(reminderService.listReminders(customerId, status)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reminder by ID")
    public ResponseEntity<ApiResponse<ReminderResponse>> getReminder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reminderService.getReminderById(id)));
    }

    @PatchMapping("/{id}/sent")
    @Operation(summary = "Mark reminder as SENT")
    public ResponseEntity<ApiResponse<ReminderResponse>> markSent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Reminder marked as sent", reminderService.markSent(id)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending reminder")
    public ResponseEntity<ApiResponse<ReminderResponse>> cancelReminder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Reminder cancelled", reminderService.cancelReminder(id)));
    }

    @PostMapping("/{id}/send-whatsapp")
    @Operation(summary = "Send reminder via WhatsApp (MANUAL copy or Cloud API depending on config)")
    public ResponseEntity<ApiResponse<ReminderSendResponse>> sendWhatsApp(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("WhatsApp reminder dispatched", reminderService.sendWhatsAppReminder(id)));
    }
}
