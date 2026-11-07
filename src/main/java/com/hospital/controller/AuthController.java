package com.hospital.controller;

import com.hospital.model.User;
import com.hospital.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        User user = service.authenticate(body.username(), body.password(), body.role());
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) oldSession.invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
        return Map.of("username", user.getUsername(), "role", user.getRole());
    }

    @GetMapping("/me")
    public Map<String, String> me(HttpSession session) {
        Object username = session.getAttribute("username");
        Object role = session.getAttribute("role");
        if (username == null || role == null) throw new UnauthenticatedException();
        return Map.of("username", username.toString(), "role", role.toString());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) { session.invalidate(); }

    public record LoginRequest(String username, String password, String role) {}

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class UnauthenticatedException extends RuntimeException {}
}
