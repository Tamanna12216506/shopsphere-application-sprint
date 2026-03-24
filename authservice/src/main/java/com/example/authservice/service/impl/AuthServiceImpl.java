package com.example.authservice.service.impl;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import com.example.authservice.entity.User;
import com.example.authservice.enums.Role;
import com.example.authservice.exception.EmailAlreadyExistsException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.AuthRepository;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private final ModelMapper modelMapper;
    @Override
    public AuthResponse register(SignupRequest signupRequest) {
        if(authRepository.existsByEmail(signupRequest.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists "+signupRequest.getEmail());
        }
        User user = modelMapper.map(signupRequest, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.valueOf(signupRequest.getRole().toUpperCase()));
        User savedUser = authRepository.save(user);
        String token = jwtUtil.generateToken(savedUser);

        AuthResponse authResponse = modelMapper.map(savedUser, AuthResponse.class);
        authResponse.setToken(token);
        return authResponse;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
        User user = authRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new ResourceNotFoundException("User not found with email: "+loginRequest.getEmail()));

        String token = jwtUtil.generateToken(user);

        AuthResponse authResponse = modelMapper.map(user, AuthResponse.class);
        authResponse.setToken(token);
        return authResponse;
    }
}
