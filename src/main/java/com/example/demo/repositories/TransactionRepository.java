package com.example.demo.repositories;

import com.example.demo.models.Transaction;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ListCrudRepository<Transaction, Long> {
    // Spring Data provides CRUD methods; keep this interface so Spring can generate the implementation.
}


