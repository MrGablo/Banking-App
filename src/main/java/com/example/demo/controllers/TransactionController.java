package com.example.demo.controllers;

import com.example.demo.common.pagination.PageResponse;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class TransactionController {
    private final TransactionService transactionService;

    @Value("${max.pagination.size}")
    private int maxPaginationSize;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer-checking")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<Transaction> transferChecking(@AuthenticationPrincipal User currentUser,
                                                        @Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transferFromCheckingToChecking(request,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE','ADMIN')")
    public ResponseEntity<Transaction> transfer( @AuthenticationPrincipal User currentUser,
                                                 @Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transferBetweenOwnAccounts(request,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE','ADMIN')")
    public PageResponse<TransactionResponse> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                transactionService.getAllTransactions(PageRequest.of(page, Math.min(size, maxPaginationSize)))
        );
    }
}
