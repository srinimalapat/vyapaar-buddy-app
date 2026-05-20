package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.CreditTransactionRequest;
import com.vyapaarbuddy.dto.response.CreditTransactionResponse;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.dto.response.TotalOutstandingCreditResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.CreditTransaction;
import com.vyapaarbuddy.entity.Customer;
import com.vyapaarbuddy.enums.CreditTransactionType;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.CreditMapper;
import com.vyapaarbuddy.mapper.CustomerMapper;
import com.vyapaarbuddy.repository.CreditTransactionRepository;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.CreditService;
import com.vyapaarbuddy.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditTransactionRepository creditTransactionRepository;
    private final CustomerRepository customerRepository;
    private final CreditMapper creditMapper;
    private final CustomerMapper customerMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public CreditTransactionResponse addCreditTransaction(CreditTransactionRequest request) {
        Business business = currentUserService.getCurrentBusiness();

        Customer customer = customerRepository.findByBusinessIdAndId(business.getId(), request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found or does not belong to your business"));

        CreditTransactionType type = request.getTransactionType();
        BigDecimal amount = request.getAmount();
        boolean allowOverPayment = Boolean.TRUE.equals(request.getAllowOverPayment());
        BigDecimal currentBalance = MoneyUtil.defaultZero(customer.getCreditBalance());

        switch (type) {
            case CREDIT_GIVEN -> customer.setCreditBalance(MoneyUtil.add(currentBalance, amount));
            case PAYMENT_RECEIVED -> {
                if (!allowOverPayment && amount.compareTo(currentBalance) > 0) {
                    throw new BadRequestException(
                            "Payment amount (" + MoneyUtil.formatINR(amount) +
                            ") cannot exceed outstanding balance (" + MoneyUtil.formatINR(currentBalance) + ")");
                }
                BigDecimal newBalance = MoneyUtil.subtract(currentBalance, amount);
                if (!allowOverPayment && MoneyUtil.isNegative(newBalance)) {
                    throw new BadRequestException("Customer credit balance cannot become negative");
                }
                customer.setCreditBalance(newBalance);
            }
            case ADJUSTMENT -> customer.setCreditBalance(MoneyUtil.add(currentBalance, amount));
            default -> throw new BadRequestException("Unsupported transaction type: " + type);
        }

        customerRepository.save(customer);

        CreditTransaction tx = CreditTransaction.builder()
                .business(business)
                .customer(customer)
                .type(type)
                .amount(amount)
                .transactionDate(request.getTransactionDate() != null
                        ? request.getTransactionDate() : LocalDate.now())
                .description(request.getDescription())
                .build();

        return creditMapper.toResponse(creditTransactionRepository.save(tx));
    }

    @Override
    @Transactional
    public CreditTransactionResponse recordPayment(CreditTransactionRequest request) {
        request.setTransactionType(CreditTransactionType.PAYMENT_RECEIVED);
        return addCreditTransaction(request);
    }

    @Override
    public List<CreditTransactionResponse> getCustomerCreditHistory(Long customerId) {
        Long businessId = currentUserService.getCurrentBusinessId();
        // verify customer belongs to business
        customerRepository.findByBusinessIdAndId(businessId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found or does not belong to your business"));
        return creditTransactionRepository
                .findByBusinessIdAndCustomerIdOrderByTransactionDateDescCreatedAtDesc(businessId, customerId)
                .stream().map(creditMapper::toResponse).toList();
    }

    @Override
    public List<CustomerResponse> getPendingCreditCustomers() {
        Long businessId = currentUserService.getCurrentBusinessId();
        return customerRepository.findByBusinessIdAndStatusAndCreditBalanceGreaterThan(
                        businessId, CustomerStatus.ACTIVE, BigDecimal.ZERO)
                .stream().map(customerMapper::toResponse).toList();
    }

    @Override
    public TotalOutstandingCreditResponse getTotalOutstanding() {
        Long businessId = currentUserService.getCurrentBusinessId();
        BigDecimal total = customerRepository.sumOutstandingCreditByBusinessId(businessId);
        Long count = customerRepository.countCustomersWithOutstandingCredit(businessId);
        return TotalOutstandingCreditResponse.builder()
                .totalOutstandingCredit(MoneyUtil.defaultZero(total))
                .customersWithPendingCredit(count)
                .build();
    }
}
