package com.example.authservice.controller;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import com.example.authservice.dto.UserResponse;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Registers a new user account (CUSTOMER or ADMIN).
     *
     * @param signupRequest registration payload
     * @return authentication response with generated token and user details
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> userSignup(@Valid @RequestBody SignupRequest signupRequest) {
        AuthResponse authResponse = authService.register(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param loginRequest login payload
     * @return authentication response with token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> userLogin(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    /**
     * Retrieves the profile of the currently authenticated user using token.
     * @return user profile details
     * its just like for testing purpose to know that our authentication is working or not
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.getProfile(email));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access successful");
    }

    /**
     *  CUSTOMER + ADMIN can access
     */
    @GetMapping("/customer")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> customerTest() {
        return ResponseEntity.ok("Customer access successful");
    }
}
