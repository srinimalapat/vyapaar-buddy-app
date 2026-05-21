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
                    JsonNode value    = change.path("value");
                    JsonNode messages = value.path("messages");
                    for (JsonNode msg : messages) {
                        String from    = msg.path("from").asText(null);
                        String msgType = msg.path("type").asText("text");
                        String waId    = msg.path("id").asText(null);

                        if (from == null) continue;

                        switch (msgType) {
                            case "image"    -> handleIncomingImageMessage(from, msg.path("image").path("id").asText(null), msg);
                            case "document" -> handleIncomingDocumentMessage(from,
                                    msg.path("document").path("id").asText(null),
                                    msg.path("document").path("filename").asText(null),
                                    msg.path("document").path("mime_type").asText(null),
                                    msg);
                            default -> {
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
                                    } catch (Exception e) {
                                        log.warn("[WEBHOOK] Auto-execute failed from={}: {}", from, e.getMessage());
                                    }
                                }
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
     * Placeholder for incoming WhatsApp IMAGE messages.
     *
     * Future implementation:
     * 1. Map fromMobileNumber → Business/User record (requires phone-to-business mapping).
     * 2. GET https://graph.facebook.com/{api-version}/{mediaId}
     *    with Authorization: Bearer {access-token} → get download URL.
     * 3. Download image bytes from URL.
     * 4. Pass to FileStockEntryService.uploadAndExtract().
     * 5. Send extracted items back to shop owner via WhatsApp.
     *
     * Do NOT auto-confirm inventory from WhatsApp until business mapping is in place.
     */
    private void handleIncomingImageMessage(String fromMobileNumber, String mediaId, JsonNode msgNode) {
        log.info("[WEBHOOK] Inbound IMAGE from={} mediaId={} — file-stock processing not yet implemented",
                fromMobileNumber, mediaId);
        // TODO: implement once phone-to-business mapping is available
    }

    /**
     * Placeholder for incoming WhatsApp DOCUMENT messages (PDF, Excel, CSV, Word, TXT).
     *
     * Future implementation:
     * 1. Map fromMobileNumber → Business/User record.
     * 2. GET https://graph.facebook.com/{api-version}/{mediaId}
     *    with Authorization: Bearer {access-token} → get download URL.
     * 3. Download document bytes from URL.
     * 4. Reconstruct as MultipartFile with correct filename and mimeType.
     * 5. Pass to FileStockEntryService.uploadAndExtract() — extractor chosen by content type.
     * 6. Send extracted items table back to shop owner for confirmation.
     *
     * Do NOT auto-confirm inventory from WhatsApp until business mapping is in place.
     */
    private void handleIncomingDocumentMessage(String fromMobileNumber, String mediaId,
                                                String filename, String mimeType, JsonNode msgNode) {
        log.info("[WEBHOOK] Inbound DOCUMENT from={} mediaId={} filename={} mimeType={} — file-stock processing not yet implemented",
                fromMobileNumber, mediaId, filename, mimeType);
        // TODO: implement once phone-to-business mapping is available
    }
}
