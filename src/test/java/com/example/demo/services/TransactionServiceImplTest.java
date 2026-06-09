package com.example.demo.services;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.TransferType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.ConflictException;
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
        fromAccount.setAbsoluteLimit(BigDecimal.ZERO);
        fromAccount.setDailyLimit(new BigDecimal("500.00"));
        fromAccount.setActive(true);
        fromAccount.setOwner(user);

        toAccount = new Account();
        toAccount.setIban("NL01INHO0222222222");
        toAccount.setType(AccountType.CHECKING);
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setAbsoluteLimit(BigDecimal.ZERO);
        toAccount.setDailyLimit(new BigDecimal("500.00"));
        toAccount.setActive(true);
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
    void getTransactionById_returnsTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Optional<Transaction> result = transactionService.getTransactionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getTransactionById_returnsEmpty() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(transactionService.getTransactionById(99L).isEmpty());
    }

    @Test
    void addTransaction_savesTransaction() {
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.addTransaction(transaction);

        assertEquals(transaction, result);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void getAllTransactions_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllTransactions(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTransactionsForUser_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findByOwnerId(1L)).thenReturn(List.of(fromAccount));
        when(transactionRepository.findByFromIbanInOrToIbanInOrderByCreatedAtDesc(
                List.of("NL01INHO0111111111"),
                List.of("NL01INHO0111111111"),
                pageable
        )).thenReturn(new PageImpl<>(List.of(transaction)));

        Page<TransactionResponse> result = transactionService.getTransactionsForUser(user, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("NL01INHO0111111111", result.getContent().get(0).fromIban());
    }

    @Test
    void getTransactionsForUser_returnsEmptyPageWhenCustomerHasNoAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findByOwnerId(1L)).thenReturn(List.of());

        Page<TransactionResponse> result = transactionService.getTransactionsForUser(user, pageable);

        assertEquals(0, result.getTotalElements());
        verify(transactionRepository, never()).findByFromIbanInOrToIbanInOrderByCreatedAtDesc(anyList(), anyList(), any());
    }

    @Test
    void getVisibleTransactions_returnsCustomerTransactions() {
        user.setRole(UserRole.CUSTOMER);
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findByOwnerId(1L)).thenReturn(List.of(fromAccount));
        when(transactionRepository.findByFromIbanInOrToIbanInOrderByCreatedAtDesc(
                List.of("NL01INHO0111111111"),
                List.of("NL01INHO0111111111"),
                pageable
        )).thenReturn(new PageImpl<>(List.of(transaction)));

        Page<TransactionResponse> result = transactionService.getVisibleTransactions(user, pageable);

        assertEquals(1, result.getTotalElements());
        verify(transactionRepository, never()).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void getVisibleTransactions_returnsAllTransactionsForEmployee() {
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(new PageImpl<>(List.of(transaction)));

        Page<TransactionResponse> result = transactionService.getVisibleTransactions(user, pageable);

        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findAllByOrderByCreatedAtDesc(pageable);
        verify(accountRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void getTransactionsForAccount_throwsNotFound() {
        when(accountRepository.existsByIban("NL01INHO0000000000")).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> transactionService.getTransactionsForAccount("NL01INHO0000000000", PageRequest.of(0, 10)));
    }

    @Test
    void getTransactionsForAccount_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.existsByIban("NL01INHO0111111111")).thenReturn(true);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByFromIbanOrToIban("NL01INHO0111111111", "NL01INHO0111111111", pageable))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.getTransactionsForAccount("NL01INHO0111111111", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void transferChecking_succeeds() {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByIban("NL01INHO0111111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIban("NL01INHO0222222222")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.sumByFromIbanAndDate(eq("NL01INHO0111111111"), any(), any()))
                .thenReturn(Optional.of(BigDecimal.ZERO));
        when(transferPolicy.validateTransfer(eq(user), eq(request), eq(fromAccount), eq(toAccount), any(), eq(TransferType.CHECKING_TO_CHECKING)))
                .thenReturn(new BigDecimal("900.00"));
        when(transactionMapper.toEntity(request, user, TransferType.CHECKING_TO_CHECKING)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.transfer(request, user, TransferType.CHECKING_TO_CHECKING);

        assertEquals(transaction, result);
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transfer_throwsNotFound() {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> transactionService.transfer(request, user, TransferType.CHECKING_TO_CHECKING));
    }

    @Test
    void transferOwnAccounts_succeeds() {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByIban("NL01INHO0111111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIban("NL01INHO0222222222")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.sumByFromIbanAndDate(eq("NL01INHO0111111111"), any(), any()))
                .thenReturn(Optional.of(BigDecimal.ZERO));
        when(transferPolicy.validateTransfer(eq(user), eq(request), eq(fromAccount), eq(toAccount), any(), eq(TransferType.OWN_ACCOUNTS)))
                .thenReturn(new BigDecimal("900.00"));
        when(transactionMapper.toEntity(request, user, TransferType.OWN_ACCOUNTS)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.transfer(request, user, TransferType.OWN_ACCOUNTS);

        assertEquals(transaction, result);
    }

    @Test
    void transfer_rejectsCustomer() {
        User customer = new User();
        customer.setId(2L);
        customer.setEmail("customer@test.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setAccounts(List.of()); // no accounts

        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(accountRepository.findByIban("NL01INHO0111111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIban("NL01INHO0222222222")).thenReturn(Optional.of(toAccount));

        assertThrows(UnauthorizedException.class,
                () -> transactionService.transfer(request, customer, TransferType.CHECKING_TO_CHECKING));
    }

    @Test
    void atmWithdraw_returnsTransactionResponse() {
        User customer = approvedCustomer();
        fromAccount.setOwner(customer);
        TransferRequest request = new TransferRequest(
                "NL01INHO0111111111",
                null,
                new BigDecimal("40.00"),
                TransferType.ATM_WITHDRAWAL,
                "ATM withdrawal"
        );
        Transaction atmTransaction = new Transaction();
        atmTransaction.setId(3L);
        atmTransaction.setFromIban("NL01INHO0111111111");
        atmTransaction.setAmount(new BigDecimal("40.00"));
        atmTransaction.setUserInitiating("Jane Doe");
        atmTransaction.setDescription("ATM withdrawal");
        atmTransaction.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findByIban("NL01INHO0111111111")).thenReturn(Optional.of(fromAccount));
        when(transactionRepository.sumByFromIbanAndDate(eq("NL01INHO0111111111"), any(), any()))
                .thenReturn(Optional.of(BigDecimal.ZERO));
        when(transferPolicy.validateAtmWithdrawal(customer, request, fromAccount, BigDecimal.ZERO))
                .thenReturn(new BigDecimal("960.00"));
        when(transactionMapper.toEntity(request, customer, TransferType.ATM_WITHDRAWAL)).thenReturn(atmTransaction);
        when(transactionRepository.save(atmTransaction)).thenReturn(atmTransaction);

        TransactionResponse result = transactionService.atmWithdraw(customer, request);

        assertEquals(3L, result.id());
        assertEquals(new BigDecimal("960.00"), fromAccount.getBalance());
        verify(accountRepository).save(fromAccount);
        verify(transactionMapper).toEntity(request, customer, TransferType.ATM_WITHDRAWAL);
    }

    @Test
    void atmWithdraw_throwsConflictWhenAccountInactive() {
        User customer = approvedCustomer();
        fromAccount.setOwner(customer);
        fromAccount.setActive(false);
        TransferRequest request = new TransferRequest(
                "NL01INHO0111111111",
                null,
                new BigDecimal("40.00"),
                TransferType.ATM_WITHDRAWAL,
                "ATM withdrawal"
        );

        when(accountRepository.findByIban("NL01INHO0111111111")).thenReturn(Optional.of(fromAccount));

        assertThrows(ConflictException.class, () -> transactionService.atmWithdraw(customer, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void atmDeposit_returnsTransactionResponse() {
        User customer = approvedCustomer();
        toAccount.setOwner(customer);
        TransferRequest request = new TransferRequest(
                null,
                "NL01INHO0222222222",
                new BigDecimal("25.00"),
                TransferType.ATM_DEPOSIT,
                "ATM deposit"
        );
        Transaction atmTransaction = new Transaction();
        atmTransaction.setId(4L);
        atmTransaction.setToIban("NL01INHO0222222222");
        atmTransaction.setAmount(new BigDecimal("25.00"));
        atmTransaction.setUserInitiating("Jane Doe");
        atmTransaction.setDescription("ATM deposit");
        atmTransaction.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findByIban("NL01INHO0222222222")).thenReturn(Optional.of(toAccount));
        when(transactionMapper.toEntity(request, customer, TransferType.ATM_DEPOSIT)).thenReturn(atmTransaction);
        when(transactionRepository.save(atmTransaction)).thenReturn(atmTransaction);

        TransactionResponse result = transactionService.atmDeposit(customer, request);

        assertEquals(4L, result.id());
        assertEquals(new BigDecimal("525.00"), toAccount.getBalance());
        verify(transferPolicy).validateAtmDeposit(customer, request, toAccount);
        verify(accountRepository).save(toAccount);
        verify(transactionMapper).toEntity(request, customer, TransferType.ATM_DEPOSIT);
    }

    @Test
    void atmDeposit_throwsNotFound() {
        User customer = approvedCustomer();
        TransferRequest request = new TransferRequest(
                null,
                "NL01INHO0000000000",
                new BigDecimal("25.00"),
                TransferType.ATM_DEPOSIT,
                "ATM deposit"
        );

        when(accountRepository.findByIban("NL01INHO0000000000")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.atmDeposit(customer, request));
    }

    private User approvedCustomer() {
        User customer = new User();
        customer.setId(2L);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane@test.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setApproved(true);
        return customer;
    }
}
