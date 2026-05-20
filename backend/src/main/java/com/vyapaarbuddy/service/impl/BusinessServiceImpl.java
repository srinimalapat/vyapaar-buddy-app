package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.BusinessRequest;
import com.vyapaarbuddy.dto.response.BusinessResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.User;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.BusinessMapper;
import com.vyapaarbuddy.repository.BusinessRepository;
import com.vyapaarbuddy.repository.UserRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BusinessResponse createBusiness(BusinessRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        if (businessRepository.existsByUserId(userId)) {
            throw new BadRequestException("Business profile already exists. Use PUT /api/v1/business/me to update it.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Business business = businessMapper.toEntity(request);
        business.setUser(user);

        Business saved = businessRepository.save(business);
        return businessMapper.toResponse(saved);
    }

    @Override
    public BusinessResponse getMyBusiness() {
        Long userId = currentUserService.getCurrentUserId();
        Business business = businessRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business profile not found. Please create one via POST /api/v1/business"));
        return businessMapper.toResponse(business);
    }

    @Override
    @Transactional
    public BusinessResponse updateMyBusiness(BusinessRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        Business business = businessRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business profile not found. Please create one via POST /api/v1/business"));

        businessMapper.updateEntity(business, request);
        Business updated = businessRepository.save(business);
        return businessMapper.toResponse(updated);
    }
}
