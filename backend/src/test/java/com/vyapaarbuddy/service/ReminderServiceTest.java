package com.vyapaarbuddy.service;

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
import com.vyapaarbuddy.service.impl.ReminderServiceImpl;
import com.vyapaarbuddy.whatsapps.ManualWhatsAppMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock private ReminderRepository reminderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ReminderMapper reminderMapper;
    @Mock private ManualWhatsAppMessageSender whatsAppMessageSender;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks
    private ReminderServiceImpl reminderService;

    private Business business;
    private Customer customer;
    private Reminder reminder;
    private ReminderResponse reminderResponse;

    @BeforeEach
    void setUp() {
        business = Business.builder().id(1L).name("Test Shop").build();
        customer = Customer.builder()
                .id(10L).name("Suresh").phone("9876543210")
                .status(CustomerStatus.ACTIVE)
                .creditBalance(BigDecimal.valueOf(500))
                .business(business).build();
        reminder = Reminder.builder()
                .id(1L).business(business).customer(customer)
                .amount(BigDecimal.valueOf(500)).status(ReminderStatus.PENDING)
                .channel(ReminderChannel.WHATSAPP_MANUAL).build();
        reminderResponse = ReminderResponse.builder()
                .id(1L).customerId(10L).customerName("Suresh")
                .amountDue(BigDecimal.valueOf(500)).status(ReminderStatus.PENDING).build();
    }

    @Test
    void generateReminder_success_defaultsFromCustomerBalance() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(customerRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(customer));
        when(reminderRepository.save(any())).thenReturn(reminder);
        when(whatsAppMessageSender.supportsChannel(ReminderChannel.WHATSAPP_MANUAL)).thenReturn(true);
        when(reminderRepository.save(reminder)).thenReturn(reminder);
        when(reminderMapper.toResponse(reminder)).thenReturn(reminderResponse);

        ReminderResponse response = reminderService.generateReminder(10L, null);

        assertNotNull(response);
        assertEquals(10L, response.getCustomerId());
        verify(whatsAppMessageSender).sendMessage(eq("9876543210"), any());
    }

    @Test
    void generateReminder_inactiveCustomer_throwsBadRequest() {
        customer.setStatus(CustomerStatus.INACTIVE);
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(customerRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(customer));

        assertThrows(BadRequestException.class,
                () -> reminderService.generateReminder(10L, null));
    }

    @Test
    void generateReminder_customMessageAndAmount() {
        ReminderRequest req = new ReminderRequest();
        req.setAmountDue(BigDecimal.valueOf(200));
        req.setMessage("Please pay Rs 200");
        req.setChannel(ReminderChannel.MANUAL);

        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(customerRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(customer));
        when(reminderRepository.save(any())).thenReturn(reminder);
        when(whatsAppMessageSender.supportsChannel(ReminderChannel.MANUAL)).thenReturn(true);
        when(reminderRepository.save(reminder)).thenReturn(reminder);
        when(reminderMapper.toResponse(reminder)).thenReturn(reminderResponse);

        reminderService.generateReminder(10L, req);

        verify(reminderRepository, atLeastOnce()).save(any());
    }

    @Test
    void markSent_updatesSentStatus() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(reminderRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(reminder)).thenReturn(reminder);
        when(reminderMapper.toResponse(reminder)).thenReturn(
                ReminderResponse.builder().id(1L).status(ReminderStatus.SENT).build());

        ReminderResponse response = reminderService.markSent(1L);

        assertEquals(ReminderStatus.SENT, reminder.getStatus());
        assertNotNull(response);
    }

    @Test
    void cancelReminder_updatesCancelledStatus() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(reminderRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(reminder)).thenReturn(reminder);
        when(reminderMapper.toResponse(reminder)).thenReturn(
                ReminderResponse.builder().id(1L).status(ReminderStatus.CANCELLED).build());

        ReminderResponse response = reminderService.cancelReminder(1L);

        assertEquals(ReminderStatus.CANCELLED, reminder.getStatus());
        assertNotNull(response);
    }

    @Test
    void cancelReminder_alreadySent_throwsBadRequest() {
        reminder.setStatus(ReminderStatus.SENT);
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(reminderRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(reminder));

        assertThrows(BadRequestException.class, () -> reminderService.cancelReminder(1L));
    }

    @Test
    void getReminderById_notFound_throwsResourceNotFound() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(reminderRepository.findByBusinessIdAndId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reminderService.getReminderById(99L));
    }

    @Test
    void bulkGenerate_generatesForAllActiveDebtors() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(customerRepository.findCustomersWithOutstandingCredit(1L)).thenReturn(List.of(customer));
        when(customerRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(customer));
        when(reminderRepository.save(any())).thenReturn(reminder);
        when(whatsAppMessageSender.supportsChannel(ReminderChannel.WHATSAPP_MANUAL)).thenReturn(true);
        when(reminderRepository.save(reminder)).thenReturn(reminder);
        when(reminderMapper.toResponse(reminder)).thenReturn(reminderResponse);

        List<ReminderResponse> responses = reminderService.bulkGenerate();

        assertEquals(1, responses.size());
    }
}
