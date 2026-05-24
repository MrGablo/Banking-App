package com.example.demo.services;

import com.example.demo.common.exception.NotFoundException;
import com.example.demo.common.enums.Currency;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.domain.policy.TransferPolicy;
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

@Service
public class TransferServiceImpl implements TransferService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferPolicy transferPolicy;

    public TransferServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository,
                               TransferPolicy transferPolicy) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferPolicy = transferPolicy;
    }

    @Override
    @Transactional
    public Transaction transferFromCheckingToChecking(User currentUser, TransferRequest request){
        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));
        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new NotFoundException("To account not found"));

        BigDecimal totalTransferedAmount = transactionRepository.sumByFromIbanAndDate(
                from.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal newBalance = transferPolicy.validateCheckingToCheckingTransfer(
                currentUser,
                request,
                from,
                to,
                totalTransferedAmount
        );

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
        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));
        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new NotFoundException("To account not found"));

        BigDecimal totalTransferedAmount = transactionRepository.sumByFromIbanAndDate(
                from.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal newBalance = transferPolicy.validateOwnAccountTransfer(
                currentUser,
                request,
                from,
                to,
                totalTransferedAmount
        );

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
}

