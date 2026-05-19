package com.example.demo.services;

import com.example.demo.common.exception.DuplicateException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.JwtService;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateException("Email already in use");
        }
        if (userRepository.findByBsn(request.bsn()).isPresent()) {
            throw new DuplicateException("BSN already in use");
        }
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new DuplicateException("Phone number already in use");
        }

        User user = userMapper.toEntity(request);
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "approved", user.isApproved()));
        String message = user.isApproved() ? "Welcome back" : "Welcome back - pending approval";
        return new AuthResponse(message, user.isApproved(), token);
    }

    @Override
    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }
}

