package com.example.demo.mapper;

import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.TransferType;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionMapperTest {

    private final TransactionMapper transactionMapper = new TransactionMapper();

    @Test
    void toEntity_mapsTransfer() {
        TransferRequest request = new TransferRequest(
                "NL01INHO0111111111", "NL01INHO0222222222", new BigDecimal("100.00"), "Rent");
        User user = new User();
        user.setFirstName("Jane");

        Transaction transaction = transactionMapper.toEntity(request, user, TransferType.OWN_ACCOUNTS);

        assertEquals("NL01INHO0111111111", transaction.getFromIban());
        assertEquals("NL01INHO0222222222", transaction.getToIban());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals("Jane", transaction.getUserInitiating());
        assertEquals(TransferType.OWN_ACCOUNTS, transaction.getTransferType());
        assertEquals(Currency.EURO, transaction.getCurrency());
        assertEquals("Rent", transaction.getDescription());
    }
}
