package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.ReminderRequest;
import com.vyapaarbuddy.dto.response.ReminderResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.Customer;
import com.vyapaarbuddy.entity.Reminder;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.enums.ReminderChannel;
import com.vyapaarbuddy.enums.ReminderStatus;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.ReminderMapper;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.repository.ReminderRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.ReminderService;
import com.vyapaarbuddy.whatsapps.ManualWhatsAppMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final CustomerRepository customerRepository;
    private final ReminderMapper reminderMapper;
    private final ManualWhatsAppMessageSender whatsAppMessageSender;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public ReminderResponse generateReminder(Long customerId, ReminderRequest request) {
        Business business = currentUserService.getCurrentBusiness();
        Customer customer = customerRepository.findByBusinessIdAndId(business.getId(), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (CustomerStatus.INACTIVE.equals(customer.getStatus())) {
            throw new BadRequestException("Cannot create reminder for inactive customer");
        }

        BigDecimal amountDue = request != null && request.getAmountDue() != null
                ? request.getAmountDue()
                : customer.getCreditBalance();
        LocalDate reminderDate = request != null && request.getReminderDate() != null
                ? request.getReminderDate()
                : LocalDate.now();
        ReminderChannel channel = request != null && request.getChannel() != null
                ? request.getChannel()
                : ReminderChannel.WHATSAPP_MANUAL;
        String message = request != null && request.getMessage() != null && !request.getMessage().isBlank()
                ? request.getMessage()
                : buildHindiMessage(customer.getName(), amountDue, business.getName());

        Reminder reminder = Reminder.builder()
                .business(business)
                .customer(customer)
                .amount(amountDue)
                .dueDate(reminderDate)
                .channel(channel)
                .status(ReminderStatus.PENDING)
                .message(message)
                .build();

        Reminder saved = reminderRepository.save(reminder);

        if (whatsAppMessageSender.supportsChannel(channel) && customer.getPhone() != null) {
            whatsAppMessageSender.sendMessage(customer.getPhone(), message);
            saved.setStatus(ReminderStatus.SENT);
            saved = reminderRepository.save(saved);
        }

        return reminderMapper.toResponse(saved);
    }

    @Override
    public List<ReminderResponse> listReminders(Long customerId, String status) {
        Long businessId = currentUserService.getCurrentBusinessId();

        ReminderStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = ReminderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid reminder status: " + status);
            }
        }

        List<Reminder> reminders;
        if (customerId != null && statusFilter != null) {
            reminders = reminderRepository.findByBusinessIdAndStatusAndCustomerId(businessId, statusFilter, customerId);
        } else if (customerId != null) {
            reminders = reminderRepository.findByBusinessIdAndCustomerId(businessId, customerId);
        } else if (statusFilter != null) {
            reminders = reminderRepository.findByBusinessIdAndStatus(businessId, statusFilter);
        } else {
            reminders = reminderRepository.findByBusinessId(businessId);
        }

        return reminders.stream().map(reminderMapper::toResponse).toList();
    }

    @Override
    public ReminderResponse getReminderById(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Reminder reminder = reminderRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        return reminderMapper.toResponse(reminder);
    }

    @Override
    @Transactional
    public ReminderResponse markSent(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Reminder reminder = reminderRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        if (ReminderStatus.CANCELLED.equals(reminder.getStatus())) {
            throw new BadRequestException("Cannot mark a cancelled reminder as sent");
        }
        reminder.setStatus(ReminderStatus.SENT);
        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }

    @Override
    @Transactional
    public ReminderResponse cancelReminder(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Reminder reminder = reminderRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
        if (ReminderStatus.SENT.equals(reminder.getStatus())) {
            throw new BadRequestException("Cannot cancel a reminder that has already been sent");
        }
        reminder.setStatus(ReminderStatus.CANCELLED);
        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }

    @Override
    @Transactional
    public List<ReminderResponse> bulkGenerate() {
        Business business = currentUserService.getCurrentBusiness();
        List<Customer> debtors = customerRepository.findCustomersWithOutstandingCredit(business.getId())
                .stream()
                .filter(c -> CustomerStatus.ACTIVE.equals(c.getStatus()))
                .toList();

        return debtors.stream()
                .map(customer -> generateReminder(customer.getId(), null))
                .toList();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private String buildHindiMessage(String customerName, BigDecimal amount, String businessName) {
        return String.format(
                "Namaste %s ji,\n\nAapka %s mein Rs %.2f baaki hai. Kripya jald se jald payment karein.\n\nDhanyawad,\n%s",
                customerName, businessName, amount, businessName);
    }
}
