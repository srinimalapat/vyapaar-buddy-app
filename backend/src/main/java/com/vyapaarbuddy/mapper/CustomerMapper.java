package com.vyapaarbuddy.mapper;

import com.vyapaarbuddy.dto.request.CustomerRequest;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.entity.Customer;
import com.vyapaarbuddy.enums.CustomerStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .name(request.getCustomerName())
                .phone(request.getMobileNumber())
                .address(request.getAddress())
                .notes(request.getNotes())
                .status(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE)
                .creditBalance(request.getTotalCreditAmount() != null ? request.getTotalCreditAmount() : BigDecimal.ZERO)
                .build();
    }

    public void updateEntity(Customer customer, CustomerRequest request) {
        customer.setName(request.getCustomerName());
        customer.setPhone(request.getMobileNumber());
        customer.setAddress(request.getAddress());
        customer.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }
        if (request.getTotalCreditAmount() != null) {
            customer.setCreditBalance(request.getTotalCreditAmount());
        }
    }

    public CustomerResponse toResponse(Customer entity) {
        if (entity == null) return null;
        return CustomerResponse.builder()
                .id(entity.getId())
                .customerName(entity.getName())
                .mobileNumber(entity.getPhone())
                .address(entity.getAddress())
                .notes(entity.getNotes())
                .totalCreditAmount(entity.getCreditBalance())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
