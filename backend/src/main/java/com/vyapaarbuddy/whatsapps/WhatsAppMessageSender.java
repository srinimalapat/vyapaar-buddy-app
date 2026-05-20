package com.vyapaarbuddy.whatsapps;

import com.vyapaarbuddy.enums.ReminderChannel;

/**
 * Interface for sending WhatsApp/notification messages.
 * Implementations decide whether they support a given channel.
 */
public interface WhatsAppMessageSender {

    void sendMessage(String phoneNumber, String message);

    void sendTemplateMessage(String phoneNumber, String templateName, Object... parameters);

    boolean supportsChannel(ReminderChannel channel);
}
