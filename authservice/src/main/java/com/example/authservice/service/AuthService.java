package com.example.authservice.service;

import com.example.authservice.dto.*;
import jakarta.validation.Valid;

public interface AuthService {
    AuthResponse register(@Valid SignupRequest signupRequest);

    AuthResponse login(@Valid LoginRequest loginRequest);

    UserResponse getProfile(String email);

}
