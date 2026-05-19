package com.example.demo.services;

import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.common.enums.Currency;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.common.enums.AccountType;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Service
public class TransferServiceImpl implements TransferService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public Transaction transferFromCheckingToChecking(User currentUser, TransferRequest request){
        if (currentUser == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        if (Objects.equals(request.fromIban(), request.toIban())) {
            throw new ConflictException("Source and destination accounts must be different");
        }

        if (!currentUser.isApproved()) {
            throw new ForbiddenException("User is not approved");
        }

        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));

        if(from.getType() != AccountType.CHECKING){
            throw new ConflictException("Only Checking Accounts Allowed");
        }

        if (from.getOwner() == null || !from.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only transfer from your own account");
        }

        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new NotFoundException("To account not found"));


        if(to.getType() != AccountType.CHECKING){
            throw new ConflictException("Only Checking Accounts Allowed");
        }

        //checking absolute Limit
        BigDecimal newBalance = from.getBalance().subtract(request.amount());
        BigDecimal minimumAllowedBalance = from.getAbsoluteLimit().negate();
        if (newBalance.compareTo(minimumAllowedBalance) < 0) {
            throw new ConflictException("Absolute limit exceeded");
        }

        //checking daily Limit
        BigDecimal totalTransferedAmount = transactionRepository.sumByFromIbanAndDate(
                from.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalCurrentTransfer = totalTransferedAmount.add(request.amount());

        if (totalCurrentTransfer.compareTo(from.getDailyLimit()) > 0){
            throw new ConflictException("Daily limit exceeded");
        }

        from.setBalance(newBalance);
        to.setBalance(to.getBalance().add(request.amount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = new Transaction();
        transaction.setFromIban(request.fromIban());
        transaction.setToIban(request.toIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(currentUser.getFirstName() + " " + currentUser.getLastName());
        transaction.setType(AccountType.CHECKING);
        transaction.setCurrency(Currency.EURO);
        transaction.setDescription(request.description());

        return transactionRepository.save(transaction);

    }

    @Override
    @Transactional
    public Transaction transferBetweenOwnAccounts(User currentUser, TransferRequest request) {
        if (currentUser == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        if (Objects.equals(request.fromIban(), request.toIban())) {
            throw new ConflictException("Source and destination accounts must be different");
        }

        if (!currentUser.isApproved()) {
            throw new ForbiddenException("User is not approved");
        }

        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));
        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new NotFoundException("To account not found"));

        if (from.getOwner() == null || to.getOwner() == null || !from.getOwner().getId().equals(currentUser.getId()) || !to.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Accounts do not belong to user");
        }

        if (!isPersonalAccount(from.getType()) || !isPersonalAccount(to.getType())) {
            throw new ConflictException("Transfers are allowed only between checking and savings accounts");
        }

        BigDecimal newBalance = from.getBalance().subtract(request.amount());
        BigDecimal minimumAllowedBalance = from.getAbsoluteLimit().negate();
        if (newBalance.compareTo(minimumAllowedBalance) < 0) {
            throw new ConflictException("Absolute limit exceeded");
        }

        BigDecimal totalTransferedAmount = transactionRepository.sumByFromIbanAndDate(
                from.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalCurrentTransfer = totalTransferedAmount.add(request.amount());

        if (totalCurrentTransfer.compareTo(from.getDailyLimit()) > 0) {
            throw new ConflictException("Daily limit exceeded");
        }

        from.setBalance(newBalance);
        to.setBalance(to.getBalance().add(request.amount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = new Transaction();
        transaction.setFromIban(from.getIban());
        transaction.setToIban(to.getIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(currentUser.getFirstName() + " " + currentUser.getLastName());
        transaction.setType(from.getType());
        transaction.setCurrency(from.getCurrency());
        transaction.setDescription(request.description());

        return transactionRepository.save(transaction);
    }

    private boolean isPersonalAccount(AccountType type) {
        return type == AccountType.CHECKING || type == AccountType.SAVINGS;
    }
}

