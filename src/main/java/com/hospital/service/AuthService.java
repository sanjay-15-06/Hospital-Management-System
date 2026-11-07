package com.hospital.service;

import com.hospital.model.User;
import com.hospital.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class AuthService {
    private final UserRepository repository;
    private final PasswordService passwords = new PasswordService();

    public AuthService(UserRepository repository) { this.repository = repository; }

    @PostConstruct
    public void seedDemoUsers() {
        if (!repository.findAll().isEmpty()) return;
        repository.save(new User(null, "admin", "ADMIN", passwords.hash("Admin123!")));
        repository.save(new User(null, "doctor", "DOCTOR", passwords.hash("Doctor123!")));
        repository.save(new User(null, "pharmacy", "PHARMACY", passwords.hash("Pharmacy123!")));
    }

    public User authenticate(String username, String password, String requestedRole) {
        if (username == null || password == null || requestedRole == null) {
            throw new IllegalArgumentException("Username, password and portal are required.");
        }
        String role = requestedRole.trim().toUpperCase(Locale.ROOT);
        if (!role.equals("ADMIN") && !role.equals("DOCTOR") && !role.equals("PHARMACY")) {
            throw new IllegalArgumentException("Select a valid login portal.");
        }
        User user = repository.findByUsername(username.trim()).orElse(null);
        if (user == null || !user.getRole().equals(role) || !passwords.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }
}
