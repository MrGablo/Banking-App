package com.example.demo.repositories;

import com.example.demo.models.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

// Legacy in-memory transaction store kept for development/seeding. Not registered as a Spring @Repository
public class InMemoryTransactionRepository {
    private final Map<Long, Transaction> transactions = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    public InMemoryTransactionRepository() {
        save(new Transaction(null, "NL01INHO0123456789", "NL02INHO0987654321", 75.00, LocalDateTime.now().minusDays(1), "j.doe@example.com"));
        save(new Transaction(null, "NL02INHO0987654321", "NL01INHO0123456789", 25.00, LocalDateTime.now().minusHours(4), "j.doe@example.com"));
    }

    public synchronized List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }

    public synchronized Optional<Transaction> findById(long id) {
        return Optional.ofNullable(transactions.get(id));
    }

    public synchronized Transaction save(Transaction transaction) {
        Long id = transaction.id() == null ? sequence.getAndIncrement() : transaction.id();
        Transaction stored = new Transaction(id, transaction.fromIban(), transaction.toIban(), transaction.amount(), transaction.timestamp(), transaction.userInitiating());
        transactions.put(id, stored);
        sequence.updateAndGet(current -> Math.max(current, id + 1));
        return stored;
    }

    public synchronized boolean deleteById(long id) {
        return transactions.remove(id) != null;
    }
}

