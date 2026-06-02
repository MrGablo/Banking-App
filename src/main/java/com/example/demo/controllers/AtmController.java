package com.example.demo.controllers;

import com.example.demo.dtos.AtmRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.services.AtmService;
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

    private final AtmService atmService;

    public AtmController(AtmService atmService) {
        this.atmService = atmService;
    }

    @PostMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> withdraw(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AtmRequest request
    ) {
        Transaction transaction = atmService.withdraw(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transaction> deposit(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AtmRequest request
    ) {
        Transaction transaction = atmService.deposit(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}