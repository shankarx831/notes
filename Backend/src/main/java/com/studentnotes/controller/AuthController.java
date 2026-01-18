package com.studentnotes.controller;

import com.studentnotes.model.User;
import com.studentnotes.repository.UserRepository;
import com.studentnotes.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            // This verifies email/password via Spring Security
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Credentials");
        }

        // Generate REAL Token
        String token = jwtUtil.generateToken(email);
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", user.getId(),
                "role", user.getRole(),
                "name", user.getName(),
                "departments", user.getAssignedDepartments() != null ? user.getAssignedDepartments() : List.of()));
    }
}