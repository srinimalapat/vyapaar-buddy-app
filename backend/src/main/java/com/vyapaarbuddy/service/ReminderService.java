package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.ReminderRequest;
import com.vyapaarbuddy.dto.response.ReminderResponse;
import com.vyapaarbuddy.dto.response.ReminderSendResponse;

import java.util.List;

public interface ReminderService {

    ReminderResponse generateReminder(Long customerId, ReminderRequest request);

    List<ReminderResponse> listReminders(Long customerId, String status);

    ReminderResponse getReminderById(Long id);

    ReminderResponse markSent(Long id);

    ReminderResponse cancelReminder(Long id);

    List<ReminderResponse> bulkGenerate();

    ReminderSendResponse sendWhatsAppReminder(Long id);
}
