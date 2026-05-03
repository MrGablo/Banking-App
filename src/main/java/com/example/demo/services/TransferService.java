package com.example.demo.services;

import com.example.demo.dtos.TransferRequest;
import com.example.demo.models.Transaction;

public interface TransferService {
    Transaction transferBetweenOwnAccounts(TransferRequest request);
}

