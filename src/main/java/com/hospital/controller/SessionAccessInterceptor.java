package com.hospital.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Map;
import java.util.Set;

@Component
public class SessionAccessInterceptor implements HandlerInterceptor {
    private static final Set<String> AUTH_PUBLIC = Set.of("/api/auth/login");
    private static final Set<String> ALL_ROLES = Set.of("ADMIN", "DOCTOR", "PHARMACY");
    private static final Map<String, Set<String>> ROLE_PATHS = Map.of(
            "/api/auth", ALL_ROLES,
            "/api/dashboard", Set.of("ADMIN"),
            "/api/patients", Set.of("ADMIN", "DOCTOR"),
            "/api/doctors", Set.of("ADMIN", "DOCTOR"),
            "/api/appointments", Set.of("ADMIN", "DOCTOR"),
            "/api/prescriptions", Set.of("DOCTOR", "PHARMACY"),
            "/api/reports", Set.of("DOCTOR"),
            "/api/pharmacy", Set.of("PHARMACY")
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || AUTH_PUBLIC.contains(path)) return true;
        Object role = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        if (role == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Please sign in to continue.\"}");
            return false;
        }
        String root = path;
        int nextSlash = path.indexOf('/', "/api/".length());
        if (nextSlash >= 0) root = path.substring(0, nextSlash);
        Set<String> allowedRoles = ROLE_PATHS.get(root);
        if (allowedRoles == null || !allowedRoles.contains(role.toString())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Your portal does not have permission for this action.\"}");
            return false;
        }
        if (root.equals("/api/patients") && role.equals("DOCTOR") && !request.getMethod().equals("GET")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Doctors can view patient records but cannot edit them.\"}");
            return false;
        }
        if (root.equals("/api/doctors") && role.equals("DOCTOR") && !request.getMethod().equals("GET")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Doctors can view doctor records but cannot edit them.\"}");
            return false;
        }
        if (root.equals("/api/appointments") && role.equals("DOCTOR")
                && !Set.of("GET", "PUT").contains(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Doctors may view and update appointments only.\"}");
            return false;
        }
        if (root.equals("/api/prescriptions") && role.equals("DOCTOR") && !request.getMethod().equals("POST")
                && !request.getMethod().equals("GET")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Doctors may create and view prescriptions only.\"}");
            return false;
        }
        if (root.equals("/api/prescriptions") && role.equals("PHARMACY")
                && !(request.getMethod().equals("GET") && path.startsWith("/api/prescriptions/"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Use pharmacy lookup to access prescriptions.\"}");
            return false;
        }
        if (root.equals("/api/reports") && role.equals("DOCTOR")
                && !Set.of("GET", "POST").contains(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Doctors may create and view patient reports only.\"}");
            return false;
        }
        return true;
    }
}
