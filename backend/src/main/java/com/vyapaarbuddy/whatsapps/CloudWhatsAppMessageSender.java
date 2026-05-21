package com.vyapaarbuddy.whatsapps;

import com.vyapaarbuddy.config.WhatsAppProperties;
import com.vyapaarbuddy.dto.response.WhatsAppSendResponse;
import com.vyapaarbuddy.enums.WhatsAppMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * WhatsApp Cloud API sender.
 * Calls Meta's Messages API when app.whatsapp.mode=CLOUD_API and cloud-api.enabled=true.
 * Remains inactive when mode=MANUAL (default).
 *
 * SECURITY: Access token is never logged. Only safe metadata is logged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CloudWhatsAppMessageSender {

    private final WhatsAppProperties properties;
    private final RestTemplate restTemplate;

    public WhatsAppSendResponse sendTextMessage(String toMobileNumber, String message) {
        WhatsAppProperties.CloudApi cfg = properties.getCloudApi();

        if (!cfg.isEnabled()) {
            return WhatsAppSendResponse.builder()
                    .success(false).provider("CLOUD_API")
                    .status("DISABLED")
                    .errorMessage("WhatsApp Cloud API is disabled. Set app.whatsapp.cloud-api.enabled=true to enable.")
                    .build();
        }

        if (cfg.getPhoneNumberId() == null || cfg.getPhoneNumberId().isBlank()) {
            return WhatsAppSendResponse.builder()
                    .success(false).provider("CLOUD_API")
                    .status("MISCONFIGURED")
                    .errorMessage("WHATSAPP_PHONE_NUMBER_ID is not configured.")
                    .build();
        }

        if (cfg.getAccessToken() == null || cfg.getAccessToken().isBlank()) {
            return WhatsAppSendResponse.builder()
                    .success(false).provider("CLOUD_API")
                    .status("MISCONFIGURED")
                    .errorMessage("WHATSAPP_ACCESS_TOKEN is not configured.")
                    .build();
        }

        String normalizedNumber = normalizeIndianMobile(toMobileNumber);
        String url = String.format("%s/%s/%s/messages",
                cfg.getBaseUrl(), cfg.getApiVersion(), cfg.getPhoneNumberId());

        log.info("[CLOUD WHATSAPP] Sending text message to: {} via phoneNumberId: {}",
                normalizedNumber, cfg.getPhoneNumberId());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cfg.getAccessToken());

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", normalizedNumber,
                    "type", "text",
                    "text", Map.of("preview_url", false, "body", message)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[CLOUD WHATSAPP] Message sent successfully to {}", normalizedNumber);
                return WhatsAppSendResponse.builder()
                        .success(true).provider("CLOUD_API")
                        .status("SENT")
                        .rawResponse(response.getBody())
                        .build();
            } else {
                log.warn("[CLOUD WHATSAPP] Non-2xx response: {} for {}", response.getStatusCode(), normalizedNumber);
                return WhatsAppSendResponse.builder()
                        .success(false).provider("CLOUD_API")
                        .status("FAILED")
                        .errorMessage("HTTP " + response.getStatusCode())
                        .rawResponse(response.getBody())
                        .build();
            }
        } catch (Exception e) {
            log.error("[CLOUD WHATSAPP] Failed to send to {}: {}", normalizedNumber, e.getMessage());
            return WhatsAppSendResponse.builder()
                    .success(false).provider("CLOUD_API")
                    .status("ERROR")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    public boolean supports(WhatsAppMode mode) {
        return mode == WhatsAppMode.CLOUD_API;
    }

    /** Normalises Indian mobile numbers to the international format expected by Meta (91XXXXXXXXXX). */
    private String normalizeIndianMobile(String mobile) {
        if (mobile == null) return "";
        String cleaned = mobile.replaceAll("[^\\d]", "");
        if (cleaned.startsWith("91") && cleaned.length() == 12) return cleaned;
        if (cleaned.length() == 10) return "91" + cleaned;
        return cleaned;
    }
}
