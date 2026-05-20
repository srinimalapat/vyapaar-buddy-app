package com.vyapaarbuddy.mapper;

import com.vyapaarbuddy.dto.response.ReminderResponse;
import com.vyapaarbuddy.entity.Reminder;
import org.springframework.stereotype.Component;

@Component
public class ReminderMapper {

    public ReminderResponse toResponse(Reminder entity) {
        if (entity == null) return null;
        return ReminderResponse.builder()
                .id(entity.getId())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .customerName(entity.getCustomer() != null ? entity.getCustomer().getName() : null)
                .customerMobileNumber(entity.getCustomer() != null ? entity.getCustomer().getPhone() : null)
                .amountDue(entity.getAmount())
                .reminderDate(entity.getDueDate())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .channel(entity.getChannel())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
