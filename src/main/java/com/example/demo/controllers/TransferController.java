package com.example.demo.controllers;

import com.example.demo.dtos.TransferRequest;
import com.example.demo.models.Transaction;
import com.example.demo.services.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) {
        try {
            Transaction transaction = transferService.transferBetweenOwnAccounts(request);
            return ResponseEntity.status(201).body(transaction);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        }
    }
}

