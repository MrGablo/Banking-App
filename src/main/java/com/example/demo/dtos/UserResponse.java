package com.example.demo.dtos;

import com.example.demo.entity.User;
import com.example.demo.common.enums.UserRole;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String bsn,
        String phoneNumber,
        UserRole role,
        boolean approved,
        boolean active
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBsn(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isApproved(),
                user.isActive()
        );
    }
}
