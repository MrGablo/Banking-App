package com.example.demo.dtos;

import com.example.demo.entity.Account;
import com.example.demo.common.enums.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        String iban,
        AccountType type,
        BigDecimal balance,
        BigDecimal absoluteLimit,
        BigDecimal dailyLimit,
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
