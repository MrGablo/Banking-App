package com.example.demo.controllers;

import com.example.demo.common.pagination.PageResponse;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.dtos.UserResponse;
import com.example.demo.services.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> getCustomersWithoutAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                employeeService.getCustomersWithoutAccounts(PageRequest.of(page, Math.min(size, 100)))
        );
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<UserResponse> approveCustomer(
            @PathVariable Long userId,
            @Valid @RequestBody ApproveCustomerRequest request) {
        UserResponse response = employeeService.approveCustomer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/accounts/{iban}/close")
    public ResponseEntity<Void> closeAccount(@PathVariable String iban) {
        employeeService.closeAccount(iban);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/accounts/{iban}/limits")
    public ResponseEntity<Void> updateLimits(
            @PathVariable String iban,
            @Valid @RequestBody UpdateLimitsRequest request) {
        employeeService.updateLimits(iban, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/accounts")
    public PageResponse<AccountResponse> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                employeeService.getAllAccounts(PageRequest.of(page, Math.min(size, 100)))
        );
    }

    @GetMapping("/transactions")
    public PageResponse<TransactionResponse> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                employeeService.getAllTransactions(PageRequest.of(page, Math.min(size, 100)))
        );
    }

    @GetMapping("/accounts/{iban}/transactions")
    public PageResponse<TransactionResponse> getAccountTransactions(
            @PathVariable String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                employeeService.getTransactionsForAccount(iban, PageRequest.of(page, Math.min(size, 100)))
        );
    }
}
