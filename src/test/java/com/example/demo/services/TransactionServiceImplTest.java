package com.example.demo.services;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.domain.policy.TransferPolicy;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransferPolicy transferPolicy;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Account fromAccount;
    private Account toAccount;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@test.com");
        user.setRole(UserRole.EMPLOYEE);
        user.setApproved(true);

        fromAccount = new Account();
        fromAccount.setIban("NL01INHO0111111111");
        fromAccount.setType(AccountType.CHECKING);
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount.setOwner(user);

        toAccount = new Account();
        toAccount.setIban("NL01INHO0222222222");
        toAccount.setType(AccountType.CHECKING);
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setOwner(user);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setFromIban("NL01INHO0111111111");
        transaction.setToIban("NL01INHO0222222222");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setUserInitiating("John");
        transaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getTransactionByIdReturnsTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Optional<Transaction> result = transactionService.getTransactionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getTransactionByIdReturnsEmptyWhenNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(transactionService.getTransactionById(99L).isEmpty());
    }

    @Test
    void addTransactionSavesAndReturns() {
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.addTransaction(transaction);

        assertEquals(transaction, result);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void deleteTransactionReturnsTrueWhenExists() {
        when(transactionRepository.existsById(1L)).thenReturn(true);

        assertTrue(transactionService.deleteTransaction(1L));
        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void deleteTransactionReturnsFalseWhenNotExists() {
        when(transactionRepository.existsById(99L)).thenReturn(false);

        assertFalse(transactionService.deleteTransaction(99L));
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void getAllTransactionsReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findAll(pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllTransactions(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTransactionsForAccountThrowsWhenAccountNotFound() {
        when(accountRepository.existsByIban("NL01INHO0000000000")).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> transactionService.getTransactionsForAccount("NL01INHO0000000000", PageRequest.of(0, 10)));
    }

    @Test
    void getTransactionsForAccountReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.existsByIban("NL01INHO0111111111")).thenReturn(true);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByFromIbanOrToIban("NL01INHO0111111111", "NL01INHO0111111111", pageable))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.getTransactionsForAccount("NL01INHO0111111111", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void transferFromCheckingToCheckingSuccess() {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByIban("NL01INHO0111111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIban("NL01INHO0222222222")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.sumByFromIbanAndDate(eq("NL01INHO0111111111"), any(), any()))
                .thenReturn(Optional.of(BigDecimal.ZERO));
        when(transferPolicy.validateCheckingToCheckingTransfer(eq(user), eq(request), eq(fromAccount), eq(toAccount), any()))
                .thenReturn(new BigDecimal("900.00"));
        when(transactionMapper.toEntity(request, user)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.transferFromCheckingToChecking(request, user);

        assertEquals(transaction, result);
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferFromCheckingToCheckingThrowsWhenUserNotFound() {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> transactionService.transferFromCheckingToChecking(request, user));
    }

    @Test
    void transferBetweenOwnAccountsSuccess() {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByIban("NL01INHO0111111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIban("NL01INHO0222222222")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.sumByFromIbanAndDate(eq("NL01INHO0111111111"), any(), any()))
                .thenReturn(Optional.of(BigDecimal.ZERO));
        when(transferPolicy.validateOwnAccountTransfer(eq(user), eq(request), eq(fromAccount), eq(toAccount), any()))
                .thenReturn(new BigDecimal("900.00"));
        when(transactionMapper.toEntity(request, user)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.transferBetweenOwnAccounts(request, user);

        assertEquals(transaction, result);
    }

    @Test
    void transferCheckingToCheckingRejectsUnauthorizedCustomer() {
        User customer = new User();
        customer.setId(2L);
        customer.setEmail("customer@test.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setAccounts(List.of()); // no accounts

        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        assertThrows(UnauthorizedException.class,
                () -> transactionService.transferFromCheckingToChecking(request, customer));
    }
}
