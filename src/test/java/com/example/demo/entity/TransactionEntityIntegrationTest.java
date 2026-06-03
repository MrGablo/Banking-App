package com.example.demo.entity;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import com.example.demo.repositories.TransactionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class TransactionEntityIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndLoad_persistsTransactionFieldsAndTimestamps() {
        Transaction saved = transactionRepository.saveAndFlush(transaction(
                "NL01INHO0123456789",
                "NL02INHO0987654321",
                "75.00"
        ));
        entityManager.clear();

        Transaction loaded = transactionRepository.findById(saved.getId()).orElseThrow();

        assertNotNull(saved.getId());
        assertEquals("NL01INHO0123456789", loaded.getFromIban());
        assertEquals("NL02INHO0987654321", loaded.getToIban());
        assertEquals(new BigDecimal("75.00"), loaded.getAmount());
        assertEquals("Jane Customer", loaded.getUserInitiating());
        assertEquals(AccountType.CHECKING, loaded.getType());
        assertEquals(Currency.EURO, loaded.getCurrency());
        assertEquals("JUnit transfer", loaded.getDescription());
        assertNotNull(loaded.getCreatedAt());
        assertNotNull(loaded.getUpdatedAt());
    }

    @Test
    void findByFromIbanOrToIban_returnsIncomingAndOutgoingTransactions() {
        transactionRepository.save(transaction("NL01INHO0123456789", "NL02INHO0987654321", "75.00"));
        transactionRepository.save(transaction("NL03INHO0123456789", "NL01INHO0123456789", "25.00"));
        transactionRepository.saveAndFlush(transaction("NL03INHO0123456789", "NL04INHO0987654321", "10.00"));

        var page = transactionRepository.findByFromIbanOrToIban(
                "NL01INHO0123456789",
                "NL01INHO0123456789",
                PageRequest.of(0, 10)
        );

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void sumByFromIbanAndDate_returnsTotalForCurrentDay() {
        transactionRepository.save(transaction("NL01INHO0123456789", "NL02INHO0987654321", "75.00"));
        transactionRepository.saveAndFlush(transaction("NL01INHO0123456789", "NL03INHO0987654321", "25.00"));

        BigDecimal total = transactionRepository.sumByFromIbanAndDate(
                "NL01INHO0123456789",
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElseThrow();

        assertEquals(new BigDecimal("100.00"), total);
    }

    private Transaction transaction(String fromIban, String toIban, String amount) {
        Transaction transaction = new Transaction();
        transaction.setFromIban(fromIban);
        transaction.setToIban(toIban);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setUserInitiating("Jane Customer");
        transaction.setType(AccountType.CHECKING);
        transaction.setCurrency(Currency.EURO);
        transaction.setDescription("JUnit transfer");
        return transaction;
    }
}
