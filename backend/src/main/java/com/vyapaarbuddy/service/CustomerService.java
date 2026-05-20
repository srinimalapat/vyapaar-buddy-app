package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.CustomerRequest;
import com.vyapaarbuddy.dto.response.CustomerResponse;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse getCustomerById(Long id);

    List<CustomerResponse> getAllCustomers(String status);

    List<CustomerResponse> searchCustomers(String query);

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    void deactivateCustomer(Long id);

    Optional<CustomerResponse> findCustomerByName(String name);
}
