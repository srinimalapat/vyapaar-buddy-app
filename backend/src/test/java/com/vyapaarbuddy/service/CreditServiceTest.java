package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.CreditTransactionRequest;
import com.vyapaarbuddy.dto.response.CreditTransactionResponse;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.dto.response.TotalOutstandingCreditResponse;
import com.vyapaarbuddy.entity.*;
import com.vyapaarbuddy.enums.CreditTransactionType;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.mapper.CreditMapper;
import com.vyapaarbuddy.mapper.CustomerMapper;
import com.vyapaarbuddy.repository.CreditTransactionRepository;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.CreditServiceImpl;
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
class CreditServiceTest {

    @Mock private CreditTransactionRepository creditTransactionRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CreditMapper creditMapper;
    @Mock private CustomerMapper customerMapper;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks
    private CreditServiceImpl creditService;

    private Business business;
    private Customer customer;

    @BeforeEach
    void setUp() {
        business = Business.builder().id(1L).name("Test Shop").build();
        customer = Customer.builder()
                .id(10L).name("Ramesh").status(CustomerStatus.ACTIVE)
                .creditBalance(new BigDecimal("1000.00")).business(business).build();
        lenient().when(currentUserService.getCurrentBusiness()).thenReturn(business);
        lenient().when(customerRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(customer));
    }

    private CreditTransactionRequest buildRequest(CreditTransactionType type,
                                                   BigDecimal amount, Boolean allowOver) {
        CreditTransactionRequest req = new CreditTransactionRequest();
        req.setCustomerId(10L);
        req.setTransactionType(type);
        req.setAmount(amount);
        req.setAllowOverPayment(allowOver);
        return req;
    }

    @Test
    void addCreditGiven_increasesCustomerBalance() {
        CreditTransaction saved = CreditTransaction.builder().id(1L)
                .type(CreditTransactionType.CREDIT_GIVEN).amount(BigDecimal.valueOf(500)).build();
        when(creditTransactionRepository.save(any())).thenReturn(saved);
        when(creditMapper.toResponse(saved)).thenReturn(CreditTransactionResponse.builder().id(1L).build());

        creditService.addCreditTransaction(buildRequest(CreditTransactionType.CREDIT_GIVEN,
                BigDecimal.valueOf(500), false));

        verify(customerRepository).save(customer);
        assertEquals(new BigDecimal("1500.00"), customer.getCreditBalance());
    }

    @Test
    void recordPaymentReceived_decreasesCustomerBalance() {
        CreditTransaction saved = CreditTransaction.builder().id(2L)
                .type(CreditTransactionType.PAYMENT_RECEIVED).amount(BigDecimal.valueOf(400)).build();
        when(creditTransactionRepository.save(any())).thenReturn(saved);
        when(creditMapper.toResponse(saved)).thenReturn(CreditTransactionResponse.builder().id(2L).build());

        creditService.addCreditTransaction(buildRequest(CreditTransactionType.PAYMENT_RECEIVED,
                BigDecimal.valueOf(400), false));

        verify(customerRepository).save(customer);
        assertEquals(new BigDecimal("600.00"), customer.getCreditBalance());
    }

    @Test
    void recordPayment_exceedsBalance_throwsBadRequest() {
        assertThrows(BadRequestException.class, () ->
                creditService.addCreditTransaction(buildRequest(CreditTransactionType.PAYMENT_RECEIVED,
                        BigDecimal.valueOf(2000), false)));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void recordPayment_exceedsBalance_allowedWithFlag() {
        CreditTransaction saved = CreditTransaction.builder().id(3L)
                .type(CreditTransactionType.PAYMENT_RECEIVED).amount(BigDecimal.valueOf(2000)).build();
        when(creditTransactionRepository.save(any())).thenReturn(saved);
        when(creditMapper.toResponse(saved)).thenReturn(CreditTransactionResponse.builder().id(3L).build());

        creditService.addCreditTransaction(buildRequest(CreditTransactionType.PAYMENT_RECEIVED,
                BigDecimal.valueOf(2000), true));

        verify(customerRepository).save(customer);
        assertEquals(new BigDecimal("-1000.00"), customer.getCreditBalance());
    }

    @Test
    void getPendingCreditCustomers_returnsActiveCustomersWithBalance() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(customerRepository.findByBusinessIdAndStatusAndCreditBalanceGreaterThan(
                1L, CustomerStatus.ACTIVE, BigDecimal.ZERO))
                .thenReturn(List.of(customer));
        when(customerMapper.toResponse(customer))
                .thenReturn(CustomerResponse.builder().id(10L).customerName("Ramesh").build());

        List<CustomerResponse> result = creditService.getPendingCreditCustomers();

        assertEquals(1, result.size());
        assertEquals("Ramesh", result.get(0).getCustomerName());
    }

    @Test
    void getTotalOutstanding_returnsAggregates() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(customerRepository.sumOutstandingCreditByBusinessId(1L))
                .thenReturn(new BigDecimal("5000.00"));
        when(customerRepository.countCustomersWithOutstandingCredit(1L)).thenReturn(3L);

        TotalOutstandingCreditResponse result = creditService.getTotalOutstanding();

        assertEquals(new BigDecimal("5000.00"), result.getTotalOutstandingCredit());
        assertEquals(3L, result.getCustomersWithPendingCredit());
    }
}
