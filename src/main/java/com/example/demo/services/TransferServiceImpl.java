package com.example.demo.services;

import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.UserRole;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.common.enums.AccountType;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

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
    public Transaction transferFromCheckingToChecking(TransferRequest request){

        if (Objects.equals(request.fromIban(), request.toIban())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        User user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isApproved()) {
            throw new IllegalStateException("User is not approved");
        }

        if(user.getRole() != UserRole.EMPLOYEE){
            throw new IllegalStateException("Only Employees are allowed");
        }

        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));

        if(from.getType() != AccountType.CHECKING){
            throw new IllegalStateException("Only Checking Accounts Allowed");
        }

        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));


        if(to.getType() != AccountType.CHECKING){
            throw new IllegalStateException("Only Checking Accounts Allowed");
        }

        if(request.amount().compareTo(from.getBalance()) > 0){
            throw new IllegalStateException("Insufficient Funds");

        }

        //checking absolute Limit
        BigDecimal newBalance = from.getBalance().subtract(request.amount());
        if (newBalance.compareTo(from.getAbsoluteLimit()) < 0) {
            throw new IllegalStateException("Absolute limit exceeded");
        }

        //checking daily Limit
        BigDecimal totalTransferedAmount = transactionRepository.sumByFromIbanAndDate(
                from.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalCurrentTransfer = totalTransferedAmount.add(request.amount());

        if (totalCurrentTransfer.compareTo(from.getDailyLimit()) > 0){
            throw new IllegalStateException("Daily limit exceeded");
        }

        from.setBalance(newBalance);
        to.setBalance(to.getBalance().add(request.amount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = new Transaction();
        transaction.setFromIban(request.fromIban());
        transaction.setToIban(request.toIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(user.getFirstName());
        transaction.setType(AccountType.CHECKING);
        transaction.setCurrency(Currency.EURO);
        transaction.setDescription(request.description());

        return transactionRepository.save(transaction);

    }

    @Override
    @Transactional
    public Transaction transferBetweenOwnAccounts(TransferRequest request) {
        if (Objects.equals(request.fromIban(), request.toIban())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

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

        BigDecimal newBalance = from.getBalance().subtract(request.amount());
        if (newBalance.compareTo(from.getAbsoluteLimit()) > 0) {
            throw new IllegalStateException("Absolute limit exceeded");
        }

        from.setBalance(newBalance);
        to.setBalance(to.getBalance().add(request.amount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = new Transaction();
        transaction.setFromIban("NL01INHO0123456789");
        transaction.setToIban("NL02INHO0987654321");
        transaction.setAmount(BigDecimal.valueOf(75.00));
        transaction.setUserInitiating("");
        transaction.setType(AccountType.CHECKING);
        transaction.setCurrency(Currency.EURO);
        transaction.setDescription("");

        return transactionRepository.save(transaction);
    }

    private boolean isPersonalAccount(AccountType type) {
        return type == AccountType.CHECKING || type == AccountType.SAVINGS;
    }
}

