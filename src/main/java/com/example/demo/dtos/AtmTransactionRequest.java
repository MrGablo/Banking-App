package com.example.demo.dtos;

import com.example.demo.common.enums.AtmTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AtmTransactionRequest(
        @NotBlank String userEmail,
        @NotBlank String iban,
        @Positive BigDecimal amount,
        @NotNull AtmTransactionType type
) {
}