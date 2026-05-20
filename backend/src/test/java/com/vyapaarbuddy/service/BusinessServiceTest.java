package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.BusinessRequest;
import com.vyapaarbuddy.dto.response.BusinessResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.User;
import com.vyapaarbuddy.enums.BusinessType;
import com.vyapaarbuddy.enums.PreferredLanguage;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.BusinessMapper;
import com.vyapaarbuddy.repository.BusinessRepository;
import com.vyapaarbuddy.repository.UserRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.BusinessServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock private BusinessRepository businessRepository;
    @Mock private BusinessMapper businessMapper;
    @Mock private CurrentUserService currentUserService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private BusinessServiceImpl businessService;

    private BusinessRequest request;
    private Business business;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("owner@test.com").name("Test Owner").build();

        request = new BusinessRequest();
        request.setOwnerName("Test Owner");
        request.setMobileNumber("9876543210");
        request.setBusinessName("Test Shop");
        request.setBusinessType(BusinessType.RETAIL);
        request.setCity("Mumbai");
        request.setState("Maharashtra");
        request.setPreferredLanguage(PreferredLanguage.HINDI);

        business = Business.builder()
                .id(1L)
                .ownerName("Test Owner")
                .name("Test Shop")
                .phone("9876543210")
                .type(BusinessType.RETAIL)
                .city("Mumbai")
                .state("Maharashtra")
                .preferredLanguage(PreferredLanguage.HINDI)
                .user(user)
                .build();
    }

    @Test
    void createBusiness_success() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(businessRepository.existsByUserId(1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(businessMapper.toEntity(request)).thenReturn(business);
        when(businessRepository.save(any(Business.class))).thenReturn(business);
        when(businessMapper.toResponse(business)).thenReturn(BusinessResponse.builder()
                .id(1L).businessName("Test Shop").build());

        BusinessResponse response = businessService.createBusiness(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(businessRepository).save(any(Business.class));
    }

    @Test
    void createBusiness_duplicateThrowsBadRequest() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(businessRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> businessService.createBusiness(request));
        verify(businessRepository, never()).save(any());
    }

    @Test
    void getMyBusiness_success() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(businessRepository.findByUserId(1L)).thenReturn(Optional.of(business));
        when(businessMapper.toResponse(business)).thenReturn(BusinessResponse.builder()
                .id(1L).businessName("Test Shop").build());

        BusinessResponse response = businessService.getMyBusiness();

        assertNotNull(response);
        assertEquals("Test Shop", response.getBusinessName());
    }

    @Test
    void getMyBusiness_notFound_throwsResourceNotFound() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(businessRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.getMyBusiness());
    }

    @Test
    void updateMyBusiness_success() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(businessRepository.findByUserId(1L)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(business);
        when(businessMapper.toResponse(business)).thenReturn(BusinessResponse.builder()
                .id(1L).businessName("Test Shop").build());

        BusinessResponse response = businessService.updateMyBusiness(request);

        assertNotNull(response);
        verify(businessMapper).updateEntity(business, request);
        verify(businessRepository).save(business);
    }
}
