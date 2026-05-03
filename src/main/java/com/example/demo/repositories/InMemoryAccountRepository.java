package com.example.demo.repositories;

import com.example.demo.models.Account;
import com.example.demo.models.AccountType;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryAccountRepository implements AccountRepository {
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public InMemoryAccountRepository() {
        accounts.put("NL01INHO0123456789", new Account("NL01INHO0123456789", AccountType.CHECKING, 1250.50, -50.00, 500.00, true));
        accounts.put("NL02INHO0987654321", new Account("NL02INHO0987654321", AccountType.SAVINGS, 4200.00, 0.00, 0.00, true));
    }

    @Override
    public synchronized List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public synchronized Optional<Account> findByIban(String iban) {
        return Optional.ofNullable(accounts.get(iban));
    }

    @Override
    public synchronized Account save(Account account) {
        accounts.put(account.iban(), account);
        return account;
    }

    @Override
    public synchronized boolean deleteByIban(String iban) {
        return accounts.remove(iban) != null;
    }
}

