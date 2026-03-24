package com.example.authservice.config;

import com.example.authservice.entity.User;
import com.example.authservice.enums.Role;
import com.example.authservice.repository.AuthRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final AuthRepository authRepository;
    @PostConstruct
    public void init() {
        String adminEmail = "admin@gmail.com";

        // check if admin already exists
        if (!authRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setUsername("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setPhoneNumber("9999988888");
            admin.setActive(true);

            authRepository.save(admin);
            System.out.println("Admin user created successfully.");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
