package com.example.demo.services;

import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;

public interface TransferService {

    Transaction transferFromCheckingToChecking(TransferRequest request);
    Transaction transferBetweenOwnAccounts(TransferRequest request);
}

