package com.example.demo.controllers;

import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.User;
import com.example.demo.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/atm")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class AtmController {

    private final TransactionService transactionService;

    public AtmController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> withdraw(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransferRequest request
    ) {
        TransactionResponse response = transactionService.atmWithdraw(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> deposit(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransferRequest request
    ) {
        TransactionResponse response = transactionService.atmDeposit(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}