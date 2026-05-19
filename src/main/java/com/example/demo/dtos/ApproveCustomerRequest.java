package com.example.demo.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ApproveCustomerRequest(
        @NotNull BigDecimal absoluteLimit,
        @NotNull @PositiveOrZero BigDecimal dailyLimit
) {
}
