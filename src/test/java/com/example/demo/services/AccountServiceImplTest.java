package com.example.demo.services;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        account = new Account();
        account.setId(1L);
        account.setIban("NL01INHO0123456789");
        account.setType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
        account.setAbsoluteLimit(new BigDecimal("0.00"));
        account.setDailyLimit(new BigDecimal("500.00"));
        account.setActive(true);
        account.setOwner(owner);
    }

    @Test
    void getAccountByIban_returnsAccount() {
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountByIban("NL01INHO0123456789");

        assertTrue(result.isPresent());
        assertEquals("NL01INHO0123456789", result.get().getIban());
    }

    @Test
    void getAccountByIban_returnsEmpty() {
        when(accountRepository.findByIban("NL01INHO0000000000")).thenReturn(Optional.empty());

        Optional<Account> result = accountService.getAccountByIban("NL01INHO0000000000");

        assertTrue(result.isEmpty());
    }

    @Test
    void addAccount_savesAccount() {
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.addAccount(account);

        assertEquals(account, result);
        verify(accountRepository).save(account);
    }

    @Test
    void closeAccount_setsInactive() {
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        accountService.closeAccount("NL01INHO0123456789");

        assertFalse(account.isActive());
        verify(accountRepository).save(account);
    }

    @Test
    void closeAccount_throwsNotFound() {
        when(accountRepository.findByIban("NL01INHO0000000000")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.closeAccount("NL01INHO0000000000"));
    }

    @Test
    void closeAccount_throwsConflict() {
        account.setActive(false);
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        assertThrows(ConflictException.class, () -> accountService.closeAccount("NL01INHO0123456789"));
    }

    @Test
    void updateLimits_updatesAndSaves() {
        UpdateLimitsRequest request = new UpdateLimitsRequest(new BigDecimal("100.00"), new BigDecimal("1000.00"));
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        accountService.updateLimits("NL01INHO0123456789", request);

        assertEquals(new BigDecimal("100.00"), account.getAbsoluteLimit());
        assertEquals(new BigDecimal("1000.00"), account.getDailyLimit());
        verify(accountRepository).save(account);
    }

    @Test
    void updateLimits_throwsNotFound() {
        UpdateLimitsRequest request = new UpdateLimitsRequest(new BigDecimal("100.00"), new BigDecimal("1000.00"));
        when(accountRepository.findByIban("NL01INHO0000000000")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.updateLimits("NL01INHO0000000000", request));
    }

    @Test
    void getAllAccounts_returnsResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(List.of(account));
        when(accountRepository.findAll(pageable)).thenReturn(page);

        Page<AccountResponse> result = accountService.getAllAccounts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("NL01INHO0123456789", result.getContent().get(0).iban());
    }

    @Test
    void getAccountsForOwner_returnsResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(List.of(account));
        when(accountRepository.findByOwnerId(1L, pageable)).thenReturn(page);

        Page<AccountResponse> result = accountService.getAccountsForOwner(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).ownerId());
    }

    @Test
    void getVisibleAccounts_returnsCustomerAccounts() {
        owner.setRole(UserRole.CUSTOMER);
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findByOwnerId(1L, pageable)).thenReturn(new PageImpl<>(List.of(account)));

        Page<AccountResponse> result = accountService.getVisibleAccounts(owner, pageable);

        assertEquals(1, result.getTotalElements());
        verify(accountRepository).findByOwnerId(1L, pageable);
        verify(accountRepository, never()).findAll(pageable);
    }

    @Test
    void getVisibleAccounts_returnsAllAccountsForEmployee() {
        owner.setRole(UserRole.EMPLOYEE);
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(account)));

        Page<AccountResponse> result = accountService.getVisibleAccounts(owner, pageable);

        assertEquals(1, result.getTotalElements());
        verify(accountRepository).findAll(pageable);
        verify(accountRepository, never()).findByOwnerId(anyLong(), any());
    }
}
