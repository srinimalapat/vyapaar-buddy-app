package com.vyapaarbuddy.whatsapps;

import com.vyapaarbuddy.enums.ReminderChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Manual WhatsApp sender — for local MVP.
 * Does NOT send real messages. Logs the message so the shop owner can
 * copy it and send manually via their WhatsApp app.
 */
@Slf4j
@Primary
@Component
public class ManualWhatsAppMessageSender implements WhatsAppMessageSender {

    @Override
    public void sendMessage(String phoneNumber, String message) {
        // In local MVP: user manually copies this message and sends via WhatsApp.
        log.info("[MANUAL WHATSAPP] To: {} | Message: {}", phoneNumber, message);
    }

    @Override
    public void sendTemplateMessage(String phoneNumber, String templateName, Object... parameters) {
        log.info("[MANUAL WHATSAPP TEMPLATE] To: {} | Template: {}", phoneNumber, templateName);
    }

    @Override
    public boolean supportsChannel(ReminderChannel channel) {
        return channel == ReminderChannel.WHATSAPP_MANUAL || channel == ReminderChannel.MANUAL;
    }
}
