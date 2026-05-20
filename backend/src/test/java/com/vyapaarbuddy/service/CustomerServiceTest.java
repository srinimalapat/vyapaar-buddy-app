package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.CustomerRequest;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.Customer;
import com.vyapaarbuddy.enums.CustomerStatus;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.CustomerMapper;
import com.vyapaarbuddy.repository.CustomerRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.CustomerServiceImpl;
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
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerMapper customerMapper;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private static final Long BUSINESS_ID = 10L;
    private static final Long OTHER_BUSINESS_ID = 99L;

    private Business business;
    private CustomerRequest request;
    private Customer customer;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        business = Business.builder().id(BUSINESS_ID).name("Test Shop").build();

        request = new CustomerRequest();
        request.setCustomerName("Ramesh Kumar");
        request.setMobileNumber("9876543210");
        request.setStatus(CustomerStatus.ACTIVE);

        customer = Customer.builder()
                .id(1L)
                .name("Ramesh Kumar")
                .phone("9876543210")
                .status(CustomerStatus.ACTIVE)
                .creditBalance(BigDecimal.ZERO)
                .business(business)
                .build();

        customerResponse = CustomerResponse.builder()
                .id(1L)
                .customerName("Ramesh Kumar")
                .mobileNumber("9876543210")
                .status(CustomerStatus.ACTIVE)
                .totalCreditAmount(BigDecimal.ZERO)
                .build();
    }

    @Test
    void createCustomer_success() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Ramesh Kumar", response.getCustomerName());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void getCustomerById_belongsToCurrentBusiness_success() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(BUSINESS_ID);
        when(customerRepository.findByBusinessIdAndId(BUSINESS_ID, 1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        CustomerResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getCustomerById_fromOtherBusiness_throwsNotFound() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(OTHER_BUSINESS_ID);
        when(customerRepository.findByBusinessIdAndId(OTHER_BUSINESS_ID, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(1L));
    }

    @Test
    void getAllCustomers_returnsOnlyCurrentBusinessCustomers() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(BUSINESS_ID);
        when(customerRepository.findByBusinessIdAndStatus(BUSINESS_ID, CustomerStatus.ACTIVE))
                .thenReturn(List.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        List<CustomerResponse> result = customerService.getAllCustomers(null);

        assertEquals(1, result.size());
        verify(customerRepository).findByBusinessIdAndStatus(BUSINESS_ID, CustomerStatus.ACTIVE);
    }

    @Test
    void searchCustomers_searchesWithinCurrentBusiness() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(BUSINESS_ID);
        when(customerRepository.searchCustomersByBusinessId(BUSINESS_ID, "ramesh"))
                .thenReturn(List.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        List<CustomerResponse> result = customerService.searchCustomers("ramesh");

        assertEquals(1, result.size());
        assertEquals("Ramesh Kumar", result.get(0).getCustomerName());
    }

    @Test
    void deactivateCustomer_setsStatusToInactive() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(BUSINESS_ID);
        when(customerRepository.findByBusinessIdAndId(BUSINESS_ID, 1L)).thenReturn(Optional.of(customer));

        customerService.deactivateCustomer(1L);

        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
        verify(customerRepository).save(customer);
    }

    @Test
    void deactivateCustomer_fromOtherBusiness_throwsNotFound() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(OTHER_BUSINESS_ID);
        when(customerRepository.findByBusinessIdAndId(OTHER_BUSINESS_ID, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.deactivateCustomer(1L));
        verify(customerRepository, never()).save(any());
    }
}
