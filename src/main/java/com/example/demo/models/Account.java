package com.example.demo.models;

public record Account(
        String iban,
        AccountType type,
        double balance,
        double absoluteLimit,
        double dailyLimit,
        boolean active
) {
}

