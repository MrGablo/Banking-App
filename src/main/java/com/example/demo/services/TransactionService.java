package com.example.demo.services;

import com.example.demo.models.Transaction;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public interface TransactionService {
    List<Transaction> getAllTransactions();

    Optional<Transaction> getTransactionById(long id);

    Transaction addTransaction(Transaction transaction);

    boolean deleteTransaction(long id);
}


