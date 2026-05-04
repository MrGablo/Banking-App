package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotBlank String userEmail,
        @NotBlank String fromIban,
        @NotBlank String toIban,
        @Positive double amount
) {
}

