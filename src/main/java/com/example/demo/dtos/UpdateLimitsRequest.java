package com.example.demo.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateLimitsRequest(
        @NotNull Double absoluteLimit,
        @NotNull @PositiveOrZero Double dailyLimit
) {
}
