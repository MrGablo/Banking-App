package com.example.demo.services;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.dtos.UserDTO;

public interface AuthService {
    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserDTO getCurrentUser(String email);
}



