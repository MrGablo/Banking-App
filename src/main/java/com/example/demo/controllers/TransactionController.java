package com.example.demo.controllers;

import com.example.demo.common.pagination.PageResponse;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.dtos.TransactionSearchRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.services.TransactionService;
import com.example.demo.services.TransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class TransactionController {
    private final TransferService transferService;
    private final TransactionService transactionService;

    public TransactionController(TransferService transferService, TransactionService transactionService) {
        this.transferService = transferService;
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer-checking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> transferChecking(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransferRequest request) {
        Transaction transaction = transferService.transferFromCheckingToChecking(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> transfer(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransferRequest request) {
        Transaction transaction = transferService.transferBetweenOwnAccounts(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    @GetMapping
    public PageResponse<TransactionResponse> getAllTransactions(
            TransactionSearchRequest filter,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.of(
                transactionService.searchTransactions(
                        filter,
                        PageRequest.of(
                                page,
                                Math.min(size, 100),
                                Sort.by("createdAt").descending()
                        )
                )
        );
    }
}
