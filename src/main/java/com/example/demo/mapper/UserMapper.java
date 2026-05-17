package com.example.demo.mapper;

import com.example.demo.dtos.RegisterRequest;
import com.example.demo.dtos.UserDTO;
import com.example.demo.models.User;
import com.example.demo.models.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        return new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.bsn(),
                request.phoneNumber(),
                hashedPassword,
                UserRole.CUSTOMER,
                false
        );
    }

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isApproved()
        );
    }
}