package com.example.demo.dtos;

import com.example.demo.common.enums.TransferType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
         String fromIban,
         String toIban,

        @NotNull
        @Positive BigDecimal amount,


         TransferType transferType,

        String description
) {
    public TransferRequest {
        if (transferType == null) {
            transferType = TransferType.CHECKING_TO_CHECKING;
        }
    }
    public TransferRequest(String fromIban, String toIban, BigDecimal amount, String description) {
        this(fromIban, toIban, amount, TransferType.CHECKING_TO_CHECKING, description);
    }
}

