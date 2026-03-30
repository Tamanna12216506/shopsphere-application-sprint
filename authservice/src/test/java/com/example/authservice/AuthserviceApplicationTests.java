package com.example.authservice;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import com.example.authservice.dto.UserResponse;
import com.example.authservice.entity.User;
import com.example.authservice.enums.Role;
import com.example.authservice.exception.EmailAlreadyExistsException;
import com.example.authservice.repository.AuthRepository;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthserviceApplicationTests {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User mockUser;
    private AuthResponse mappedAuthResponse;
    private UserResponse mappedUserResponse;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("John Doe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setPhoneNumber("8989978956");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("John Doe");
        mockUser.setEmail("john@example.com");
        mockUser.setPassword("password123");
        mockUser.setPhoneNumber("9876543210");
        mockUser.setRole(Role.CUSTOMER);

        mappedAuthResponse = new AuthResponse();
        mappedAuthResponse.setUser(UserResponse.builder()
                .id(1L)
                .username("John Doe")
                .email("john@example.com")
                .role("CUSTOMER")
                .phoneNumber("9876543210")
                .build());

        mappedUserResponse = UserResponse.builder()
                .id(1L)
                .username("John Doe")
                .email("john@example.com")
                .role("CUSTOMER")
                .phoneNumber("9876543210")
                .build();
    }

    // ── TEST 1: Successful signup ────────────────────────────────────────────
    @Test
    void signup_ShouldReturnAuthResponse_WhenEmailNotRegistered() {
        when(authRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(modelMapper.map(any(SignupRequest.class), eq(User.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("hashedPassword");
        when(authRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock.jwt.token");
        when(modelMapper.map(any(User.class), eq(AuthResponse.class))).thenReturn(mappedAuthResponse);

        AuthResponse response = authService.register(signupRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("john@example.com", response.getUser().getEmail());
        assertEquals("CUSTOMER", response.getUser().getRole());
        assertNull(response.getMessage());

        verify(authRepository, times(1)).save(any(User.class));
    }

    // ── TEST 2: Signup with duplicate email ──────────────────────────────────
    @Test
    void signup_ShouldThrowException_WhenEmailAlreadyExists() {
        when(authRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(signupRequest)
        );

        assertTrue(exception.getMessage().contains("john@example.com"));
        verify(authRepository, never()).save(any(User.class));
    }

    // ── TEST 3: Successful login ─────────────────────────────────────────────
    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(authRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock.jwt.token");
        when(modelMapper.map(any(User.class), eq(AuthResponse.class))).thenReturn(mappedAuthResponse);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("john@example.com", response.getUser().getEmail());
        assertNull(response.getMessage());
    }

    // ── TEST 4: Login with wrong credentials ─────────────────────────────────
    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );
    }

    // ── TEST 5: Get profile ──────────────────────────────────────────────────
    @Test
    void getProfile_ShouldReturnProfile_WhenUserExists() {
        when(authRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(modelMapper.map(any(User.class), eq(UserResponse.class))).thenReturn(mappedUserResponse);

        UserResponse profile = authService.getProfile("john@example.com");

        assertNotNull(profile);
        assertEquals("John Doe", profile.getUsername());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("CUSTOMER", profile.getRole());
    }
}

