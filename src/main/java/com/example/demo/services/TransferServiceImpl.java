package com.example.demo.services;

import com.example.demo.dtos.TransferRequest;
import com.example.demo.models.Account;
import com.example.demo.models.AccountType;
import com.example.demo.models.Transaction;
import com.example.demo.models.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransferServiceImpl implements TransferService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferServiceImpl(UserRepository userRepository, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public Transaction transferBetweenOwnAccounts(TransferRequest request) {
        User user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isApproved()) {
            throw new IllegalStateException("User is not approved");
        }

        Account from = accountRepository.findById(request.fromIban())
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        Account to = accountRepository.findById(request.toIban())
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));

        if (from.getOwner() == null || to.getOwner() == null || !from.getOwner().getId().equals(user.getId()) || !to.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("Accounts do not belong to user");
        }

        if (!isPersonalAccount(from.getType()) || !isPersonalAccount(to.getType())) {
            throw new IllegalStateException("Transfers are allowed only between checking and savings accounts");
        }

        double newBalance = from.getBalance() - request.amount();
        if (newBalance < from.getAbsoluteLimit()) {
            throw new IllegalStateException("Absolute limit exceeded");
        }

        from.setBalance(newBalance);
        to.setBalance(to.getBalance() + request.amount());

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = new Transaction(null, from.getIban(), to.getIban(), request.amount(), LocalDateTime.now(), user.getEmail());
        return transactionRepository.save(transaction);
    }

    private boolean isPersonalAccount(AccountType type) {
        return type == AccountType.CHECKING || type == AccountType.SAVINGS;
    }
}

