package com.vyapaarbuddy.mapper;

import com.vyapaarbuddy.dto.request.BusinessRequest;
import com.vyapaarbuddy.dto.response.BusinessResponse;
import com.vyapaarbuddy.entity.Business;
import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public Business toEntity(BusinessRequest request) {
        return Business.builder()
                .ownerName(request.getOwnerName())
                .name(request.getBusinessName())
                .phone(request.getMobileNumber())
                .type(request.getBusinessType())
                .city(request.getCity())
                .state(request.getState())
                .preferredLanguage(request.getPreferredLanguage())
                .address(request.getAddress())
                .pinCode(request.getPinCode())
                .gstNumber(request.getGstNumber())
                .build();
    }

    public void updateEntity(Business business, BusinessRequest request) {
        business.setOwnerName(request.getOwnerName());
        business.setName(request.getBusinessName());
        business.setPhone(request.getMobileNumber());
        business.setType(request.getBusinessType());
        business.setCity(request.getCity());
        business.setState(request.getState());
        business.setPreferredLanguage(request.getPreferredLanguage());
        business.setAddress(request.getAddress());
        business.setPinCode(request.getPinCode());
        business.setGstNumber(request.getGstNumber());
    }

    public BusinessResponse toResponse(Business entity) {
        if (entity == null) return null;
        return BusinessResponse.builder()
                .id(entity.getId())
                .ownerName(entity.getOwnerName())
                .mobileNumber(entity.getPhone())
                .businessName(entity.getName())
                .businessType(entity.getType())
                .city(entity.getCity())
                .state(entity.getState())
                .preferredLanguage(entity.getPreferredLanguage())
                .address(entity.getAddress())
                .pinCode(entity.getPinCode())
                .gstNumber(entity.getGstNumber())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
