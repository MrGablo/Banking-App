package com.example.demo.controllers;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.services.AuthService;
import com.example.demo.services.JwtService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(201).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(409).body("A user with the provided email or BSN already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String email = jwtService.extractSubject(token);
            
            return ResponseEntity.ok(authService.getCurrentUser(email));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}



