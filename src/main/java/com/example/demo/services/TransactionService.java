package com.example.demo.services;

import com.example.demo.common.enums.TransferType;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.dtos.TransactionSearchRequest;
import com.example.demo.entity.User;
import com.example.demo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@SuppressWarnings("unused")
public interface TransactionService {

    Optional<Transaction> getTransactionById(long id);

    Transaction addTransaction(Transaction transaction);


    public Page<TransactionResponse> getAllTransactions(Pageable pageable);

    public Page<TransactionResponse> getTransactionsForUser(User currentUser, Pageable pageable);

    Page<TransactionResponse> getVisibleTransactions(User currentUser, Pageable pageable);

    public Page<TransactionResponse> getTransactionsForAccount(String iban, Pageable pageable);

    Page<TransactionResponse> searchTransactions(
            User currentUser,
            TransactionSearchRequest filter,
            Pageable pageable
    );

    Transaction transfer(TransferRequest request, User currentUser, TransferType transferType);

    TransactionResponse atmWithdraw(User currentUser, TransferRequest request);

    TransactionResponse atmDeposit(User currentUser, TransferRequest request);
}


