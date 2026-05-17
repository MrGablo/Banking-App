package com.example.demo.services;

import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.entity.Account;
import com.example.demo.repositories.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> getAccountByIban(String iban) {
        return accountRepository.findById(iban);
    }

    @Override
    public Account addAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public boolean deleteAccount(String iban) {
        if (accountRepository.existsById(iban)) {
            accountRepository.deleteById(iban);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void closeAccount(String iban) {
        //requireEmployee();

        Account account = accountRepository.findById(iban)
                .orElseThrow(() -> new NotFoundException("Account not found: " + iban));

        if (!account.isActive()) {
            throw new ConflictException("Account is already closed");
        }

        account.setActive(false);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateLimits(String iban, UpdateLimitsRequest request) {
        //requireEmployee();

        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new NotFoundException("Account not found: " + iban));

        account.setAbsoluteLimit(request.absoluteLimit());
        account.setDailyLimit(request.dailyLimit());
        accountRepository.save(account);
    }

    @Override
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        //requireEmployee();
        return accountRepository.findAll(pageable)
                .map(AccountResponse::from);
    }

//    private void requireEmployee() {
//        if (authContext.getCurrentUserRole() != UserRole.EMPLOYEE) {
//            throw new ForbiddenException("Employee role required");
//        }
//    }
}

