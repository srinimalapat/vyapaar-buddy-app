package com.vyapaarbuddy.whatsapps;

import com.vyapaarbuddy.enums.ReminderChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub for future WhatsApp Cloud API integration.
 * TODO: Add WhatsApp Cloud API token (env: WHATSAPP_CLOUD_API_TOKEN)
 * TODO: Add phone number ID (env: WHATSAPP_PHONE_NUMBER_ID)
 * TODO: Add webhook verification token (env: WHATSAPP_WEBHOOK_VERIFY_TOKEN)
 * TODO: Add message template support via Meta Business API
 * TODO: Implement real HTTP call to https://graph.facebook.com/v18.0/{phone-number-id}/messages
 */
@Slf4j
@Component
public class FutureCloudWhatsAppMessageSender implements WhatsAppMessageSender {

    @Override
    public void sendMessage(String phoneNumber, String message) {
        // TODO: Call WhatsApp Cloud API
        log.debug("[CLOUD WHATSAPP STUB] Would send to {}: {}", phoneNumber, message);
    }

    @Override
    public void sendTemplateMessage(String phoneNumber, String templateName, Object... parameters) {
        // TODO: Call WhatsApp Cloud API with template
        log.debug("[CLOUD WHATSAPP STUB] Would send template {} to {}", templateName, phoneNumber);
    }

    @Override
    public boolean supportsChannel(ReminderChannel channel) {
        return channel == ReminderChannel.WHATSAPP;
    }
}
