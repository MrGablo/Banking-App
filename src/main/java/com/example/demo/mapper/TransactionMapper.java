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

        transaction.setAmount(request.amount());
        transaction.setUserInitiating(user.getFirstName() + " " + user.getLastName());
        transaction.setTransferType(transferType);
        transaction.setCurrency(Currency.EURO);

        if (transferType == TransferType.ATM_WITHDRAWAL) {
            transaction.setFromIban(request.fromIban());
            transaction.setToIban(null);
            transaction.setDescription(
                    request.description() != null ? request.description() : "ATM withdrawal"
            );
        } else if (transferType == TransferType.ATM_DEPOSIT) {
            transaction.setFromIban(null);
            transaction.setToIban(request.toIban());
            transaction.setDescription(
                    request.description() != null ? request.description() : "ATM deposit"
            );
        } else {
            transaction.setFromIban(request.fromIban());
            transaction.setToIban(request.toIban());
            transaction.setDescription(request.description());
        }

        return transaction;
    }
}