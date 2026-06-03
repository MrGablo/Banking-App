package com.example.demo.services;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.domain.policy.TransferPolicy;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferPolicy transferPolicy;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Test
    void transferFromCheckingToChecking_updatesBalancesAndStoresTransaction() {
        User user = user();
        Account from = account("NL01INHO0123456789", AccountType.CHECKING, new BigDecimal("100.00"));
        Account to = account("NL02INHO0987654321", AccountType.CHECKING, new BigDecimal("25.00"));
        TransferRequest request = new TransferRequest(from.getIban(), to.getIban(), new BigDecimal("40.00"), "Rent");
        Transaction savedTransaction = new Transaction();

        when(accountRepository.findByIban(from.getIban())).thenReturn(Optional.of(from));
        when(accountRepository.findByIban(to.getIban())).thenReturn(Optional.of(to));
        when(transactionRepository.sumByFromIbanAndDate(any(), any(), any())).thenReturn(Optional.of(BigDecimal.ZERO));
        when(transferPolicy.validateCheckingToCheckingTransfer(user, request, from, to, BigDecimal.ZERO))
                .thenReturn(new BigDecimal("60.00"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        Transaction result = transferService.transferFromCheckingToChecking(user, request);

        assertEquals(savedTransaction, result);
        assertEquals(new BigDecimal("60.00"), from.getBalance());
        assertEquals(new BigDecimal("65.00"), to.getBalance());
        verify(accountRepository).save(from);
        verify(accountRepository).save(to);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferBetweenOwnAccounts_whenToAccountMissing_throwsNotFoundException() {
        Account from = account("NL01INHO0123456789", AccountType.CHECKING, new BigDecimal("100.00"));
        TransferRequest request = new TransferRequest(from.getIban(), "missing", new BigDecimal("10.00"), "Move");

        when(accountRepository.findByIban(from.getIban())).thenReturn(Optional.of(from));
        when(accountRepository.findByIban("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transferService.transferBetweenOwnAccounts(user(), request));
    }

    @Test
    void transferBetweenOwnAccounts_usesFromAccountTypeAndCurrencyOnTransaction() {
        User user = user();
        Account from = account("NL01INHO0123456789", AccountType.SAVINGS, new BigDecimal("100.00"));
        Account to = account("NL02INHO0987654321", AccountType.CHECKING, new BigDecimal("25.00"));
        TransferRequest request = new TransferRequest(from.getIban(), to.getIban(), new BigDecimal("15.00"), "Savings");

        when(accountRepository.findByIban(from.getIban())).thenReturn(Optional.of(from));
        when(accountRepository.findByIban(to.getIban())).thenReturn(Optional.of(to));
        when(transactionRepository.sumByFromIbanAndDate(any(), any(), any())).thenReturn(Optional.empty());
        when(transferPolicy.validateOwnAccountTransfer(user, request, from, to, BigDecimal.ZERO))
                .thenReturn(new BigDecimal("85.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transaction = transferService.transferBetweenOwnAccounts(user, request);

        assertEquals(AccountType.SAVINGS, transaction.getType());
        assertEquals(Currency.EURO, transaction.getCurrency());
        assertEquals("Jane Customer", transaction.getUserInitiating());
        assertEquals(new BigDecimal("85.00"), from.getBalance());
        assertEquals(new BigDecimal("40.00"), to.getBalance());
    }

    private Account account(String iban, AccountType type, BigDecimal balance) {
        Account account = new Account();
        account.setIban(iban);
        account.setType(type);
        account.setBalance(balance);
        account.setCurrency(Currency.EURO);
        return account;
    }

    private User user() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Customer");
        user.setApproved(true);
        return user;
    }
}
