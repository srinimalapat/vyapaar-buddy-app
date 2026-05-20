package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.LoginRequest;
import com.vyapaarbuddy.dto.request.RegisterRequest;
import com.vyapaarbuddy.dto.response.AuthResponse;

/**
 * Service interface for authentication operations.
 * TODO: Define login method signature
 * TODO: Define register method signature
 * TODO: Define token refresh method signature
 */
public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);
}
