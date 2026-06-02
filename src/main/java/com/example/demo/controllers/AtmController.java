package com.example.demo.controllers;

import com.example.demo.dtos.AtmTransactionRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.services.AtmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Transaction> withdraw(@Valid @RequestBody AtmTransactionRequest request) {
        Transaction transaction = atmService.withdraw(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@Valid @RequestBody AtmTransactionRequest request) {
        Transaction transaction = atmService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}