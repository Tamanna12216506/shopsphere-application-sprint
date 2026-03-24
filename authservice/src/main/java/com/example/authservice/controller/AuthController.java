package com.example.authservice.controller;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    //Register a user(Customer or ADMIN)
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse>  userSignup(@Valid @RequestBody SignupRequest signupRequest){
        AuthResponse authResponse = authService.register(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    //Login with email & password
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> userLogin(@Valid @RequestBody LoginRequest loginRequest){
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }
}
