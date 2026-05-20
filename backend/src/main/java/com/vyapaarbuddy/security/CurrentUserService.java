package com.vyapaarbuddy.security;

import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.User;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.exception.UnauthorizedException;
import com.vyapaarbuddy.repository.BusinessRepository;
import com.vyapaarbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public UserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    public Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    public Business getCurrentBusiness() {
        Long userId = getCurrentUserId();
        return businessRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found for current user. Please create your business profile first."));
    }

    public Long getCurrentBusinessId() {
        return getCurrentBusiness().getId();
    }
}
