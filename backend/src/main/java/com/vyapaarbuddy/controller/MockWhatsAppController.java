package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.request.MockWhatsAppRequest;
import com.vyapaarbuddy.dto.response.ApiResponse;
import com.vyapaarbuddy.dto.response.MockCommandResponse;
import com.vyapaarbuddy.service.MockWhatsAppParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mock-whatsapp")
@RequiredArgsConstructor
@Tag(name = "Mock WhatsApp", description = "Parse and execute WhatsApp-style text commands (local MVP)")
@SecurityRequirement(name = "bearerAuth")
public class MockWhatsAppController {

    private final MockWhatsAppParserService mockWhatsAppParserService;

    @PostMapping("/parse")
    @Operation(summary = "Parse a WhatsApp-style message — returns command without executing it",
               description = "Examples: \"Sale Ramesh rice 2kg 120 cash\", \"Udhaar Suresh 500\", \"Payment Ramesh 300\", \"Stock add sugar 10kg 45\", \"Report today\"")
    public ResponseEntity<ApiResponse<MockCommandResponse>> parseMessage(
            @Valid @RequestBody MockWhatsAppRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mockWhatsAppParserService.parseMessage(request)));
    }

    @PostMapping("/execute")
    @Operation(summary = "Parse and execute a WhatsApp-style message",
               description = "Parses the message and, if valid, performs the operation (create sale, add credit, record payment, update stock, or show report).")
    public ResponseEntity<ApiResponse<MockCommandResponse>> executeMessage(
            @Valid @RequestBody MockWhatsAppRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mockWhatsAppParserService.executeMessage(request)));
    }
}
