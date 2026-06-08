package com.example.demo.util;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.entity.Account;
import com.example.demo.repositories.AccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountUtilTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AccountUtil accountUtil = new AccountUtil(accountRepository);

    @Test
    void newAccount_createsActiveAccount() {
        when(accountRepository.existsByIban(anyString())).thenReturn(false);

        Account account = accountUtil.newAccount(
                new BigDecimal("0.00"), new BigDecimal("500.00"), AccountType.CHECKING);

        assertTrue(account.getIban().matches("NL\\d{2}INHO0\\d{9}"));
        assertEquals(new BigDecimal("0.00"), account.getAbsoluteLimit());
        assertEquals(new BigDecimal("500.00"), account.getDailyLimit());
        assertEquals(AccountType.CHECKING, account.getType());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertTrue(account.isActive());
    }

    @Test
    void generateUniqueIban_throwsAfterCollisions() {
        when(accountRepository.existsByIban(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, accountUtil::generateUniqueIban);
    }

    @Test
    void generateUniqueIban_returnsIban() {
        when(accountRepository.existsByIban(anyString())).thenReturn(false);

        String iban = accountUtil.generateUniqueIban();

        assertFalse(iban.isBlank());
        assertTrue(iban.startsWith("NL"));
    }
}
