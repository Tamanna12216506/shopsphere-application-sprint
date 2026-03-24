package com.example.authservice.service;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import jakarta.validation.Valid;

public interface AuthService {
    AuthResponse register(@Valid SignupRequest signupRequest);

    AuthResponse login(@Valid LoginRequest loginRequest);
}
