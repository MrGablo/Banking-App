package com.example.demo.dtos;

import com.example.demo.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String fromIban,
        String toIban,
        BigDecimal amount,
        LocalDateTime timestamp,
        String userInitiating
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFromIban(),
                transaction.getToIban(),
                transaction.getAmount(),
                LocalDateTime.now(),
                transaction.getUserInitiating()
        );
    }
}
