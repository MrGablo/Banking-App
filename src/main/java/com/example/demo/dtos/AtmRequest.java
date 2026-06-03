package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AtmRequest(
        @NotBlank String iban,
        @NotNull @Positive BigDecimal amount
) {
}