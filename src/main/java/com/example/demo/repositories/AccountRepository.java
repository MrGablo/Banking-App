package com.example.demo.repositories;

import com.example.demo.models.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findAll();

    Optional<Account> findByIban(String iban);

    Account save(Account account);

    boolean deleteByIban(String iban);
}

