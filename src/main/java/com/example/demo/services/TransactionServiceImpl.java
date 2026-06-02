package com.example.demo.services;

import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransactionSearchRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Transaction> getTransactionById(long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public boolean deleteTransaction(long id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        //requireEmployee();
        return transactionRepository.findAll(pageable)
                .map(TransactionResponse::from);
    }


    @Override
    public Page<TransactionResponse> getTransactionsForAccount(String iban, Pageable pageable) {
        //requireEmployee();

        if (!accountRepository.existsByIban(iban)) {
            throw new NotFoundException("Account not found: " + iban);
        }

        return transactionRepository.findByFromIbanOrToIban(iban, iban, pageable)
                .map(TransactionResponse::from);
    }

    @Override
    public Page<TransactionResponse> searchTransactions(
            TransactionSearchRequest filter,
            Pageable pageable
    ) {
        return transactionRepository.searchTransactions(filter, pageable)
                .map(TransactionResponse::from);
    }

//    private void requireEmployee() {
//        if (authContext.getCurrentUserRole() != UserRole.EMPLOYEE) {
//            throw new ForbiddenException("Employee role required");
//        }
//    }
}

