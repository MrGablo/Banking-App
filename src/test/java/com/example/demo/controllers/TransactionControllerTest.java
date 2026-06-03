package com.example.demo.controllers;

import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.services.TransactionService;
import com.example.demo.services.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransferService transferService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void transferChecking_returnsCreatedTransaction() {
        User user = new User();
        TransferRequest request = transferRequest();
        Transaction transaction = new Transaction();
        when(transferService.transferFromCheckingToChecking(user, request)).thenReturn(transaction);

        var response = transactionController.transferChecking(user, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(transaction, response.getBody());
    }

    @Test
    void transfer_returnsCreatedTransaction() {
        User user = new User();
        TransferRequest request = transferRequest();
        Transaction transaction = new Transaction();
        when(transferService.transferBetweenOwnAccounts(user, request)).thenReturn(transaction);

        var response = transactionController.transfer(user, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(transaction, response.getBody());
    }

    @Test
    void getAllTransactions_capsPageSizeAtOneHundred() {
        TransactionResponse transaction = new TransactionResponse(
                1L,
                "NL01INHO0123456789",
                "NL02INHO0987654321",
                new BigDecimal("20.00"),
                null,
                "Jane Customer"
        );
        when(transactionService.getAllTransactions(PageRequest.of(0, 100)))
                .thenReturn(new PageImpl<>(List.of(transaction), PageRequest.of(0, 100), 1));

        var response = transactionController.getAllTransactions(0, 500);

        assertEquals(1, response.totalElements());
        assertEquals(100, response.size());
        verify(transactionService).getAllTransactions(PageRequest.of(0, 100));
    }

    private TransferRequest transferRequest() {
        return new TransferRequest(
                "NL01INHO0123456789",
                "NL02INHO0987654321",
                new BigDecimal("20.00"),
                "JUnit transfer"
        );
    }
}
