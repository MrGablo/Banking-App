package com.example.demo.services;

import com.example.demo.common.enums.Currency;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.AtmTransactionRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AtmService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AtmService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction withdraw(AtmTransactionRequest request) {
        User user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Account account = accountRepository.findByIban(request.iban())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!user.isApproved()) {
            throw new ForbiddenException("User is not approved");
        }

        if (!account.isActive()) {
            throw new ConflictException("Account is not active");
        }

        if (account.getOwner() == null || !account.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("Account does not belong to user");
        }

        BigDecimal newBalance = account.getBalance().subtract(request.amount());

        if (newBalance.compareTo(account.getAbsoluteLimit()) < 0) {
            throw new ConflictException("Absolute limit exceeded");
        }

        BigDecimal totalTransferredToday = transactionRepository.sumByFromIbanAndDate(
                account.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalAfterWithdrawal = totalTransferredToday.add(request.amount());

        if (totalAfterWithdrawal.compareTo(account.getDailyLimit()) > 0) {
            throw new ConflictException("Daily limit exceeded");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setFromIban(account.getIban());
        transaction.setToIban("ATM");
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(user.getFirstName() + " " + user.getLastName());
        transaction.setType(account.getType());
        transaction.setCurrency(account.getCurrency() != null ? account.getCurrency() : Currency.EURO);
        transaction.setDescription("ATM withdrawal");

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction deposit(AtmTransactionRequest request) {
        User user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Account account = accountRepository.findByIban(request.iban())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!user.isApproved()) {
            throw new ForbiddenException("User is not approved");
        }

        if (!account.isActive()) {
            throw new ConflictException("Account is not active");
        }

        if (account.getOwner() == null || !account.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("Account does not belong to user");
        }

        BigDecimal newBalance = account.getBalance().add(request.amount());

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setFromIban("ATM");
        transaction.setToIban(account.getIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(user.getFirstName() + " " + user.getLastName());
        transaction.setType(account.getType());
        transaction.setCurrency(account.getCurrency() != null ? account.getCurrency() : Currency.EURO);
        transaction.setDescription("ATM deposit");

        return transactionRepository.save(transaction);
    }
}