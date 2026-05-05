package com.example.demo.dtos;

import com.example.demo.models.Transaction;

import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String fromIban,
        String toIban,
        double amount,
        LocalDateTime timestamp,
        String userInitiating
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFromIban(),
                transaction.getToIban(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getUserInitiating()
        );
    }
}
