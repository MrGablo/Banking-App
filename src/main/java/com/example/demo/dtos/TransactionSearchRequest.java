package com.example.demo.dtos;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionSearchRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate,

        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal exactAmount,
        String iban
) {
}