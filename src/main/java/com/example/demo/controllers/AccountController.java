package com.example.demo.controllers;

import com.example.demo.common.enums.UserRole;
import com.example.demo.common.pagination.PageResponse;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.MessageResponse;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.entity.User;
import com.example.demo.services.AccountService;
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
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Value("${max.pagination.size}")
    private int maxPaginationSize;

    public AccountController(AccountService accountService, TransactionService transactionService){
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping("/{iban}/close")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<MessageResponse> closeAccount(@PathVariable String iban) {
        accountService.closeAccount(iban);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Account successfully closed"));
    }

    @PutMapping("/{iban}/limits")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<MessageResponse> updateLimits(
            @PathVariable String iban,
            @Valid @RequestBody UpdateLimitsRequest request) {
        accountService.updateLimits(iban, request);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Limits successfully updated"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN')")
    public PageResponse<AccountResponse> getAllAccounts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, maxPaginationSize));

        if (currentUser.getRole() == UserRole.CUSTOMER) {
            return PageResponse.of(accountService.getAccountsForOwner(currentUser.getId(), pageRequest));
        }

        return PageResponse.of(
                accountService.getAllAccounts(pageRequest)
        );
    }

    @GetMapping("/{iban}/transactions")
    public PageResponse<TransactionResponse> getAccountTransactions(
            @PathVariable String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                transactionService.getTransactionsForAccount(iban, PageRequest.of(page, Math.min(size, maxPaginationSize)))
        );
    }
}
