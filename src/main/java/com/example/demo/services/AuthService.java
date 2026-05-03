package com.example.demo.services;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}

