package com.example.demo.controllers;

import com.example.demo.common.pagination.PageResponse;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.services.AccountService;
import com.example.demo.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService){
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping("/{iban}/close")
    public ResponseEntity<Void> closeAccount(@PathVariable String iban) {
        accountService.closeAccount(iban);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{iban}/limits")
    public ResponseEntity<Void> updateLimits(
            @PathVariable String iban,
            @Valid @RequestBody UpdateLimitsRequest request) {
        accountService.updateLimits(iban, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public PageResponse<AccountResponse> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                accountService.getAllAccounts(PageRequest.of(page, Math.min(size, 100)))
        );
    }

    @GetMapping("/{iban}/transactions")
    public PageResponse<TransactionResponse> getAccountTransactions(
            @PathVariable String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                transactionService.getTransactionsForAccount(iban, PageRequest.of(page, Math.min(size, 100)))
        );
    }
}
