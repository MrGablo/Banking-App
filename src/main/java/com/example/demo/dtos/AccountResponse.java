package com.example.demo.dtos;

import com.example.demo.models.Account;
import com.example.demo.models.AccountType;

public record AccountResponse(
        String iban,
        AccountType type,
        double balance,
        double absoluteLimit,
        double dailyLimit,
        boolean active,
        Long ownerId,
        String ownerName
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getIban(),
                account.getType(),
                account.getBalance(),
                account.getAbsoluteLimit(),
                account.getDailyLimit(),
                account.isActive(),
                account.getOwner().getId(),
                account.getOwner().getFirstName() + " " + account.getOwner().getLastName()
        );
    }
}
