package com.example.demo.repositories;

import com.example.demo.models.Account;
import com.example.demo.models.AccountType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Legacy in-memory data holder kept for development/seed usage but no longer exposes a Spring @Repository
public class InMemoryAccountRepository {
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public InMemoryAccountRepository() {
        accounts.put("NL01INHO0123456789", new Account("NL01INHO0123456789", AccountType.CHECKING, 1250.50, -50.00, 500.00, true));
        accounts.put("NL02INHO0987654321", new Account("NL02INHO0987654321", AccountType.SAVINGS, 4200.00, 0.00, 0.00, true));
    }

    public synchronized List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    public synchronized Optional<Account> findByIban(String iban) {
        return Optional.ofNullable(accounts.get(iban));
    }

    public synchronized Account save(Account account) {
        accounts.put(account.iban(), account);
        return account;
    }

    public synchronized boolean deleteByIban(String iban) {
        return accounts.remove(iban) != null;
    }
}

