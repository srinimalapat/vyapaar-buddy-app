package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.CustomerRequest;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.Customer;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.CustomerMapper;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Business business = currentUserService.getCurrentBusiness();
        Customer customer = customerMapper.toEntity(request);
        customer.setBusiness(business);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Customer customer = customerRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return customerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers(String status) {
        Long businessId = currentUserService.getCurrentBusinessId();
        List<Customer> customers;
        if (status != null && !status.isBlank()) {
            CustomerStatus customerStatus;
            try {
                customerStatus = CustomerStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                customerStatus = CustomerStatus.ACTIVE;
            }
            customers = customerRepository.findByBusinessIdAndStatus(businessId, customerStatus);
        } else {
            customers = customerRepository.findByBusinessIdAndStatus(businessId, CustomerStatus.ACTIVE);
        }
        return customers.stream().map(customerMapper::toResponse).toList();
    }

    @Override
    public List<CustomerResponse> searchCustomers(String query) {
        Long businessId = currentUserService.getCurrentBusinessId();
        List<Customer> customers = customerRepository.searchCustomersByBusinessId(businessId, query.trim());
        return customers.stream().map(customerMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Customer customer = customerRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerMapper.updateEntity(customer, request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public Optional<CustomerResponse> findCustomerByName(String name) {
        Long businessId = currentUserService.getCurrentBusinessId();
        List<Customer> results = customerRepository.searchCustomersByBusinessId(businessId, name.trim());
        // prefer exact match
        return results.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .or(() -> results.stream().findFirst())
                .map(customerMapper::toResponse);
    }

    @Override
    @Transactional
    public void deactivateCustomer(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Customer customer = customerRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customer.setStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }
}
