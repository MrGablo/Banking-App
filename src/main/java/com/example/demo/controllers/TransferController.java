package com.example.demo.controllers;

import com.example.demo.common.pagination.PageResponse;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.services.TransactionService;
import com.example.demo.services.TransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class TransferController {
    private final TransferService transferService;
    private final TransactionService transactionService;

    public TransferController(TransferService transferService, TransactionService transactionService) {
        this.transferService = transferService;
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer-checking")
    public ResponseEntity<Transaction> transferChecking(@Valid @RequestBody TransferRequest request) {
        Transaction transaction = transferService.transferFromCheckingToChecking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@Valid @RequestBody TransferRequest request) {
        Transaction transaction = transferService.transferBetweenOwnAccounts(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping
    public PageResponse<TransactionResponse> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                transactionService.getAllTransactions(PageRequest.of(page, Math.min(size, 100)))
        );
    }
}
