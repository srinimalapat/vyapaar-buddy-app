package com.vyapaarbuddy.controller;

import com.vyapaarbuddy.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for WhatsApp webhook integration.
 * This will be used when integrating with WhatsApp Cloud API.
 * TODO: Implement webhook verification endpoint
 * TODO: Implement webhook message receiving endpoint
 * TODO: Add webhook security validation
 * TODO: Implement message processing logic
 */
@RestController
@RequestMapping("/api/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        // TODO: Implement webhook verification for WhatsApp Cloud API
        return ResponseEntity.ok(challenge);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> receiveWebhook(@RequestBody String payload) {
        // TODO: Implement webhook message receiving and processing
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Webhook received - implement in next phase")
                .build());
    }
}
