package com.example.demo.controllers;

import com.example.demo.common.enums.UserRole;
import com.example.demo.common.enums.TransferType;
import com.example.demo.common.pagination.PageResponse;
import org.springframework.data.domain.Pageable;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.dtos.TransactionSearchRequest;
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
import org.springframework.data.domain.Sort;


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
        Transaction transaction = transactionService.transfer(request, currentUser, TransferType.CHECKING_TO_CHECKING);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE','ADMIN')")
    public ResponseEntity<Transaction> transfer( @AuthenticationPrincipal User currentUser,
                                                 @Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transfer(request, currentUser, TransferType.OWN_ACCOUNTS);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','EMPLOYEE','ADMIN')")
    public PageResponse<TransactionResponse> getAllTransactions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, maxPaginationSize));

        if (currentUser.getRole() == UserRole.CUSTOMER) {
            return PageResponse.of(transactionService.getTransactionsForUser(currentUser, pageRequest));
        }

        return PageResponse.of(
                transactionService.getAllTransactions(pageRequest)
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    public PageResponse<TransactionResponse> searchTransactions(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute TransactionSearchRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.by("createdAt").descending()
        );

        return PageResponse.of(
                transactionService.searchTransactions(currentUser, filter, pageable)
        );
    }
}
