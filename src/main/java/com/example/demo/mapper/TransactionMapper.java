package com.example.demo.mapper;

import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.TransferType;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransferRequest request, User user, TransferType transferType) {
        Transaction transaction = new Transaction();
        transaction.setFromIban(request.fromIban());
        transaction.setToIban(request.toIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(user.getFirstName());
        transaction.setTransferType(transferType);
        transaction.setCurrency(Currency.EURO);
        transaction.setDescription(request.description());

        return transaction;
    }

}
