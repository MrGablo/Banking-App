package com.example.demo.services;

import com.example.demo.common.exception.NotFoundException;
import com.example.demo.entity.Transaction;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void existingTransactionDeletes() {
        when(transactionRepository.existsById(10L)).thenReturn(true);

        boolean deleted = transactionService.deleteTransaction(10L);

        assertTrue(deleted);
        verify(transactionRepository).deleteById(10L);
    }

    @Test
    void missingAccountThrows() {
        when(accountRepository.existsByIban("missing")).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> transactionService.getTransactionsForAccount("missing", PageRequest.of(0, 20)));
    }

    @Test
    void accountTransactionsMapped() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setFromIban("NL01INHO0123456789");
        transaction.setToIban("NL02INHO0987654321");
        transaction.setAmount(new BigDecimal("20.00"));
        transaction.setUserInitiating("Jane Customer");

        PageRequest pageable = PageRequest.of(0, 20);
        when(accountRepository.existsByIban("NL01INHO0123456789")).thenReturn(true);
        when(transactionRepository.findByFromIbanOrToIban("NL01INHO0123456789", "NL01INHO0123456789", pageable))
                .thenReturn(new PageImpl<>(List.of(transaction), pageable, 1));

        var page = transactionService.getTransactionsForAccount("NL01INHO0123456789", pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("NL02INHO0987654321", page.getContent().getFirst().toIban());
    }
}
