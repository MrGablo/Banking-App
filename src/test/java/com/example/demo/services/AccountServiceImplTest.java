package com.example.demo.services;

import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.entity.Account;
import com.example.demo.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void deleteAccount_whenIbanExists_deletesAndReturnsTrue() {
        when(accountRepository.existsByIban("NL01INHO0123456789")).thenReturn(true);

        boolean deleted = accountService.deleteAccount("NL01INHO0123456789");

        assertTrue(deleted);
        verify(accountRepository).deleteByIban("NL01INHO0123456789");
    }

    @Test
    void deleteAccount_whenIbanMissing_returnsFalse() {
        when(accountRepository.existsByIban("NL01INHO0123456789")).thenReturn(false);

        boolean deleted = accountService.deleteAccount("NL01INHO0123456789");

        assertFalse(deleted);
        verify(accountRepository).existsByIban("NL01INHO0123456789");
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void closeAccount_whenActive_setsInactiveAndSaves() {
        Account account = new Account();
        account.setActive(true);
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        accountService.closeAccount("NL01INHO0123456789");

        assertFalse(account.isActive());
        verify(accountRepository).save(account);
    }

    @Test
    void closeAccount_whenMissing_throwsNotFoundException() {
        when(accountRepository.findByIban("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.closeAccount("missing"));
    }

    @Test
    void closeAccount_whenAlreadyClosed_throwsConflictException() {
        Account account = new Account();
        account.setActive(false);
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        assertThrows(ConflictException.class, () -> accountService.closeAccount("NL01INHO0123456789"));
    }

    @Test
    void updateLimits_updatesAbsoluteAndDailyLimits() {
        Account account = new Account();
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        accountService.updateLimits(
                "NL01INHO0123456789",
                new UpdateLimitsRequest(new BigDecimal("200.00"), new BigDecimal("750.00"))
        );

        assertTrue(new BigDecimal("200.00").compareTo(account.getAbsoluteLimit()) == 0);
        assertTrue(new BigDecimal("750.00").compareTo(account.getDailyLimit()) == 0);
        verify(accountRepository).save(account);
    }
}
