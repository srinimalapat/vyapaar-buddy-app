package com.vyapaarbuddy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyapaarbuddy.config.WhatsAppProperties;
import com.vyapaarbuddy.dto.request.MockWhatsAppRequest;
import com.vyapaarbuddy.entity.WhatsAppMessageLog;
import com.vyapaarbuddy.enums.WhatsAppMessageDirection;
import com.vyapaarbuddy.repository.WhatsAppMessageLogRepository;
import com.vyapaarbuddy.service.MockWhatsAppParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WhatsAppProperties properties;
    private final MockWhatsAppParserService parserService;
    private final WhatsAppMessageLogRepository messageLogRepository;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        String expected = properties.getCloudApi().getWebhookVerifyToken();
        if ("subscribe".equals(mode) && expected != null && expected.equals(verifyToken)) {
            log.info("[WEBHOOK] Verification successful");
            return ResponseEntity.ok(challenge);
        }
        log.warn("[WEBHOOK] Verification failed: mode={}", mode);
        return ResponseEntity.status(403).body("Forbidden");
    }

    // TODO: Add X-Hub-Signature-256 HMAC verification before processing payloads in production
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody JsonNode payload) {
        try {
            JsonNode entries = payload.path("entry");
            for (JsonNode entry : entries) {
                for (JsonNode change : entry.path("changes")) {
                    JsonNode value = change.path("value");
                    JsonNode messages = value.path("messages");
                    for (JsonNode msg : messages) {
                        String from    = msg.path("from").asText(null);
                        String msgType = msg.path("type").asText("text");
                        String waId    = msg.path("id").asText(null);

                        if (from == null) continue;

                        if ("image".equals(msgType)) {
                            String mediaId = msg.path("image").path("id").asText(null);
                            handleIncomingImageMessage(from, mediaId, msg);
                            continue;
                        }

                        String text = msg.path("text").path("body").asText(null);
                        if (text == null) continue;

                        log.info("[WEBHOOK] Inbound text from={} waId={}", from, waId);

                        WhatsAppMessageLog logEntry = WhatsAppMessageLog.builder()
                                .mobileNumber(from)
                                .direction(WhatsAppMessageDirection.INBOUND)
                                .waMessageId(waId)
                                .messageBody(text)
                                .messageType("text")
                                .status("RECEIVED")
                                .rawPayload(payload.toString())
                                .build();
                        messageLogRepository.save(logEntry);

                        if (properties.getWebhook().isAutoExecuteCommands()) {
                            try {
                                MockWhatsAppRequest req = new MockWhatsAppRequest();
                                req.setMessage(text);
                                parserService.executeMessage(req);
                                log.info("[WEBHOOK] Auto-executed command for message from={}", from);
                            } catch (Exception e) {
                                log.warn("[WEBHOOK] Auto-execute failed for from={}: {}", from, e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[WEBHOOK] Error processing payload: {}", e.getMessage());
        }
        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    /**
     * Placeholder for incoming WhatsApp image messages.
     *
     * Future implementation steps:
     * 1. Map fromMobileNumber to a Business/User record (requires phone-to-business mapping table).
     * 2. Call Meta media endpoint: GET https://graph.facebook.com/{api-version}/{mediaId}
     *    with Authorization: Bearer {access-token} to get download URL.
     * 3. Download the image bytes using the returned URL.
     * 4. Pass downloaded file to PhotoStockEntryService.uploadAndExtract().
     * 5. Send a confirmation WhatsApp message back to the shop owner with extracted items.
     *
     * Important: Do NOT update inventory automatically from WhatsApp image until
     * business mapping is implemented and owner confirmation is received.
     */
    private void handleIncomingImageMessage(String fromMobileNumber, String mediaId, JsonNode messageNode) {
        // Safe metadata only — never log media content or access tokens
        log.info("[WEBHOOK] Inbound image from={} mediaId={} (processing not yet implemented)",
                fromMobileNumber, mediaId);
        // TODO: Implement when business-to-phone mapping is in place
    }
}
