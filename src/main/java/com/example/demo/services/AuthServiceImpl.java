package com.example.demo.services;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.models.User;
import com.example.demo.models.UserRole;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.bsn(),
                request.phoneNumber(),
                request.password(),
                UserRole.CUSTOMER,
                false
        );
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getPasswordHash().equals(request.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String message = user.isApproved() ? "Welcome back" : "Welcome back - pending approval";
        return new AuthResponse(message, user.isApproved());
    }
}

