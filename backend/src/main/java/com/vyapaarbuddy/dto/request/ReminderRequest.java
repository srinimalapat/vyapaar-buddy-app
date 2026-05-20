package com.vyapaarbuddy.dto.request;

import com.vyapaarbuddy.enums.ReminderChannel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReminderRequest {

    private BigDecimal amountDue;

    private LocalDate reminderDate;

    private String message;

    private ReminderChannel channel;
}
