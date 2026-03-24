package com.example.authservice.service.impl;

import com.example.authservice.dto.*;
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

    /// SIGNUP
    @Override
    public AuthResponse register(SignupRequest signupRequest) {

        //// Checking if email already exist or not
        if(authRepository.existsByEmail(signupRequest.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists "+signupRequest.getEmail());
        }
        /// User entity from signup request
        User user = modelMapper.map(signupRequest, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRole(Role.valueOf(signupRequest.getRole().toUpperCase()));
        user.setRole(Role.CUSTOMER);
        /// saved to database
        User savedUser = authRepository.save(user);

        /// generating jwt token for the registered user
        String token = jwtUtil.generateToken(savedUser);

        /// return auth response with user details and token
        AuthResponse authResponse = modelMapper.map(savedUser, AuthResponse.class);
        authResponse.setToken(token);
        return authResponse;
    }


    //// LOGIN
    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        /// Authenticate use spring security's authentication manager
        /// it will internally call CustomerUserDetailsService to load user by email and check password with BCryptPasswordEncoder
        /// throws BadCredentialsException if authentication fails
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));

        /// if authentication passes, load the user
        User user = authRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new ResourceNotFoundException("User not found with email: "+loginRequest.getEmail()));

        /// generate jwt token
        String token = jwtUtil.generateToken(user);

        /// return response with user detail with token
        AuthResponse authResponse = modelMapper.map(user, AuthResponse.class);
        authResponse.setToken(token);
        return authResponse;
    }

    @Override
    public UserResponse getProfile(String email) {
        User user = authRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found with email: "+email));
        return modelMapper.map(user, UserResponse.class);
    }


}
