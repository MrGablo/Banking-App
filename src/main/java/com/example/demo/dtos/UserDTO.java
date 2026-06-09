package com.example.demo.dtos;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String role,
        boolean approved,
        boolean active
) {
}

