package com.example.demo.controllers;

import com.example.demo.common.enums.AccountType;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.services.AccountService;
import com.example.demo.services.TransactionService;
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
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountController accountController;

    @Test
    void closeAccount_returnsNoContent() {
        var response = accountController.closeAccount("NL01INHO0123456789");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService).closeAccount("NL01INHO0123456789");
    }

    @Test
    void updateLimits_returnsNoContent() {
        UpdateLimitsRequest request = new UpdateLimitsRequest(new BigDecimal("100.00"), new BigDecimal("500.00"));

        var response = accountController.updateLimits("NL01INHO0123456789", request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService).updateLimits("NL01INHO0123456789", request);
    }

    @Test
    void getAllAccounts_capsPageSizeAtOneHundred() {
        AccountResponse account = new AccountResponse(
                "NL01INHO0123456789",
                AccountType.CHECKING,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                true,
                1L,
                "Jane Customer"
        );
        when(accountService.getAllAccounts(PageRequest.of(0, 100)))
                .thenReturn(new PageImpl<>(List.of(account), PageRequest.of(0, 100), 1));

        var response = accountController.getAllAccounts(0, 500);

        assertEquals(1, response.totalElements());
        assertEquals(100, response.size());
    }

    @Test
    void getAccountTransactions_delegatesToTransactionService() {
        TransactionResponse transaction = new TransactionResponse(
                1L,
                "NL01INHO0123456789",
                "NL02INHO0987654321",
                new BigDecimal("10.00"),
                null,
                "Jane Customer"
        );
        when(transactionService.getTransactionsForAccount("NL01INHO0123456789", PageRequest.of(1, 20)))
                .thenReturn(new PageImpl<>(List.of(transaction), PageRequest.of(1, 20), 1));

        var response = accountController.getAccountTransactions("NL01INHO0123456789", 1, 20);

        assertEquals(1, response.content().size());
        assertEquals(1, response.page());
    }
}
