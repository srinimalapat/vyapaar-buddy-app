package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.LoginRequest;
import com.vyapaarbuddy.dto.request.RegisterRequest;
import com.vyapaarbuddy.dto.response.AuthResponse;
import com.vyapaarbuddy.entity.User;
import com.vyapaarbuddy.enums.UserRole;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.repository.UserRepository;
import com.vyapaarbuddy.security.JwtService;
import com.vyapaarbuddy.security.UserPrincipal;
import com.vyapaarbuddy.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserPrincipal principal = UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .role(user.getRole())
                .build();

        String token = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : UserRole.USER)
                .build();

        User saved = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .password(saved.getPassword())
                .name(saved.getName())
                .role(saved.getRole())
                .build();

        String token = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .build();
    }
}
