package com.example.demo.services;

import com.example.demo.models.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<Account> getAllAccounts();

    Optional<Account> getAccountByIban(String iban);

    Account addAccount(Account account);

    boolean deleteAccount(String iban);
}

