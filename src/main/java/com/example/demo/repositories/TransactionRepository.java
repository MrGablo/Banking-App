package com.example.demo.repositories;

import com.example.demo.models.Transaction;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public interface TransactionRepository {
    List<Transaction> findAll();

    Optional<Transaction> findById(long id);

    Transaction save(Transaction transaction);

    boolean deleteById(long id);
}


