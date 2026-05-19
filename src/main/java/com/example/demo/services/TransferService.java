package com.example.demo.services;

import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;

public interface TransferService {

    Transaction transferFromCheckingToChecking(User currentUser, TransferRequest request);
    Transaction transferBetweenOwnAccounts(User currentUser, TransferRequest request);
}

