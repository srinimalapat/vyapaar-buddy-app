package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.ReminderChannel;
import com.vyapaarbuddy.enums.ReminderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerMobileNumber;
    private BigDecimal amountDue;
    private LocalDate reminderDate;
    private String message;
    private ReminderStatus status;
    private ReminderChannel channel;
    private LocalDateTime createdAt;
}
