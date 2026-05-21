package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.response.WhatsAppSendResponse;

public interface WhatsAppSenderService {

    WhatsAppSendResponse sendTextMessage(String toMobileNumber, String message);

    WhatsAppSendResponse sendReminderMessage(String toMobileNumber, String message);
}
