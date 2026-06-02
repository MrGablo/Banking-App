package com.example.demo.services;

import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@SuppressWarnings("unused")
public interface TransactionService {

    Optional<Transaction> getTransactionById(long id);

    Transaction addTransaction(Transaction transaction);

    boolean deleteTransaction(long id);

    public Page<TransactionResponse> getAllTransactions(Pageable pageable);

    public Page<TransactionResponse> getTransactionsForAccount(String iban, Pageable pageable);

    Transaction transferFromCheckingToChecking(TransferRequest request);
    Transaction transferBetweenOwnAccounts(TransferRequest request);
}


