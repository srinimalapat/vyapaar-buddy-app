package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.CreditTransactionRequest;
import com.vyapaarbuddy.dto.response.CreditTransactionResponse;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.dto.response.TotalOutstandingCreditResponse;

import java.util.List;

public interface CreditService {

    CreditTransactionResponse addCreditTransaction(CreditTransactionRequest request);

    CreditTransactionResponse recordPayment(CreditTransactionRequest request);

    List<CreditTransactionResponse> getCustomerCreditHistory(Long customerId);

    List<CustomerResponse> getPendingCreditCustomers();

    TotalOutstandingCreditResponse getTotalOutstanding();
}
