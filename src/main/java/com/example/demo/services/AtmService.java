package com.example.demo.services;

import com.example.demo.common.enums.Currency;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.domain.policy.TransferPolicy;
import com.example.demo.dtos.AtmRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AtmService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferPolicy transferPolicy;

    public AtmService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransferPolicy transferPolicy
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferPolicy = transferPolicy;
    }

    @Transactional
    public Transaction withdraw(User currentUser, AtmRequest request) {
        Account account = accountRepository.findByIban(request.iban())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.isActive()) {
            throw new ConflictException("Account is not active");
        }

        transferPolicy.enforceAuthenticatedUser(currentUser);
        transferPolicy.enforceApprovedUser(currentUser);
        transferPolicy.enforceSourceAccountOwnership(currentUser, account);

        LocalDate today = LocalDate.now();
        BigDecimal totalTransferredToday = transactionRepository.sumByFromIbanAndDate(
                account.getIban(),
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal newBalance = transferPolicy.enforceAbsoluteLimit(account, request.amount());
        transferPolicy.enforceDailyLimit(account, totalTransferredToday, request.amount());

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setFromIban(account.getIban());
        transaction.setToIban("ATM");
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(currentUser.getFirstName() + " " + currentUser.getLastName());
        transaction.setType(account.getType());
        transaction.setCurrency(account.getCurrency() != null ? account.getCurrency() : Currency.EURO);
        transaction.setDescription("ATM withdrawal");

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction deposit(User currentUser, AtmRequest request) {
        Account account = accountRepository.findByIban(request.iban())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.isActive()) {
            throw new ConflictException("Account is not active");
        }

        transferPolicy.enforceAuthenticatedUser(currentUser);
        transferPolicy.enforceApprovedUser(currentUser);
        transferPolicy.enforceSourceAccountOwnership(currentUser, account);

        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setFromIban("ATM");
        transaction.setToIban(account.getIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(currentUser.getFirstName() + " " + currentUser.getLastName());
        transaction.setType(account.getType());
        transaction.setCurrency(account.getCurrency() != null ? account.getCurrency() : Currency.EURO);
        transaction.setDescription("ATM deposit");

        return transactionRepository.save(transaction);
    }
}