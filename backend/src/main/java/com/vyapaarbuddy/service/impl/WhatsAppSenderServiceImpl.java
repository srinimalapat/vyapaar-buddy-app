package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.config.WhatsAppProperties;
import com.vyapaarbuddy.dto.response.WhatsAppSendResponse;
import com.vyapaarbuddy.enums.WhatsAppMode;
import com.vyapaarbuddy.service.WhatsAppSenderService;
import com.vyapaarbuddy.whatsapps.CloudWhatsAppMessageSender;
import com.vyapaarbuddy.whatsapps.ManualWhatsAppMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppSenderServiceImpl implements WhatsAppSenderService {

    private final WhatsAppProperties properties;
    private final ManualWhatsAppMessageSender manualSender;
    private final CloudWhatsAppMessageSender cloudSender;

    @Override
    public WhatsAppSendResponse sendTextMessage(String toMobileNumber, String message) {
        return dispatch(toMobileNumber, message);
    }

    @Override
    public WhatsAppSendResponse sendReminderMessage(String toMobileNumber, String message) {
        return dispatch(toMobileNumber, message);
    }

    private WhatsAppSendResponse dispatch(String toMobileNumber, String message) {
        WhatsAppMode mode = properties.getMode();
        log.info("[WHATSAPP] Dispatching message via mode={} to={}", mode, toMobileNumber);

        if (mode == WhatsAppMode.CLOUD_API && properties.getCloudApi().isEnabled()) {
            return cloudSender.sendTextMessage(toMobileNumber, message);
        }

        if (mode == WhatsAppMode.MANUAL) {
            manualSender.sendMessage(toMobileNumber, message);
            return WhatsAppSendResponse.builder()
                    .success(true)
                    .provider("MANUAL")
                    .messageId("manual-copy")
                    .status("READY_TO_COPY")
                    .build();
        }

        // CLOUD_API mode but not enabled
        return WhatsAppSendResponse.builder()
                .success(false)
                .provider("CLOUD_API")
                .status("DISABLED")
                .errorMessage("WhatsApp Cloud API is not enabled. Set app.whatsapp.cloud-api.enabled=true.")
                .build();
    }
}
