package com.example.demo.mapper;

import com.example.demo.common.enums.UserRole;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entity.User;
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

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setBsn(request.bsn());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(false);
        user.setPasswordHash(hashedPassword);

        return user;
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