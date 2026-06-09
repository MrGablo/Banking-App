package com.example.demo.services;

import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AccountService {

    Optional<Account> getAccountByIban(String iban);

    Account addAccount(Account account);

    public void closeAccount(String iban);

    public void updateLimits(String iban, UpdateLimitsRequest request);

    public Page<AccountResponse> getAllAccounts(Pageable pageable);

    public Page<AccountResponse> getAccountsForOwner(Long ownerId, Pageable pageable);

    Page<AccountResponse> getVisibleAccounts(User currentUser, Pageable pageable);
}

