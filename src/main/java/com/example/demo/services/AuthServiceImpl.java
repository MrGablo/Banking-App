package com.example.demo.services;

import com.example.demo.common.exception.DuplicateException;
import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.common.enums.UserRole;
import com.example.demo.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.findByBsn(request.bsn()).isPresent()) {
            throw new IllegalArgumentException("BSN already in use");
        }
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new IllegalArgumentException("PhoneNumber already in use");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setBsn(request.bsn());
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(false);
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "approved", user.isApproved()));
        String message = user.isApproved() ? "Welcome back" : "Welcome back - pending approval";
        return new AuthResponse(message, user.isApproved(), token);
    }
}

