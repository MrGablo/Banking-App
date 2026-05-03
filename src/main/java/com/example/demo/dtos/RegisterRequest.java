package com.example.demo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^[0-9]{8,9}$") String bsn,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{8,15}$") String phoneNumber,
        @NotBlank @Size(min = 6, max = 100) String password
) {
}

