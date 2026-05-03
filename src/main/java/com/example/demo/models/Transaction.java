package com.example.demo.models;

import java.time.LocalDateTime;

public record Transaction(
        Long id,
        String fromIban,
        String toIban,
        double amount,
        LocalDateTime timestamp,
        String userInitiating
) {
}

