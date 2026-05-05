package com.example.demo.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ApproveCustomerRequest(
        @NotNull Double absoluteLimit,
        @NotNull @PositiveOrZero Double dailyLimit
) {
}
