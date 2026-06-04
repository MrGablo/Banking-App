package com.example.demo.entity;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.UserRole;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AccountEntityIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void accountPersists() {
        User owner = userRepository.save(customer("account-owner@example.com", "123456780"));

        Account account = new Account();
        account.setIban("NL91INHO0123456789");
        account.setType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("250.00"));
        account.setAbsoluteLimit(new BigDecimal("100.00"));
        account.setDailyLimit(new BigDecimal("500.00"));
        account.setActive(true);
        account.setCurrency(Currency.EURO);
        account.setOwner(owner);

        Account saved = accountRepository.saveAndFlush(account);
        entityManager.clear();

        Account loaded = accountRepository.findByIban("NL91INHO0123456789").orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(AccountType.CHECKING, loaded.getType());
        assertEquals(new BigDecimal("250.00"), loaded.getBalance());
        assertEquals(new BigDecimal("100.00"), loaded.getAbsoluteLimit());
        assertEquals(new BigDecimal("500.00"), loaded.getDailyLimit());
        assertTrue(loaded.isActive());
        assertEquals(Currency.EURO, loaded.getCurrency());
        assertEquals(owner.getId(), loaded.getOwner().getId());
        assertNotNull(loaded.getCreatedAt());
        assertNotNull(loaded.getUpdatedAt());
    }

    @Test
    void duplicateIbanThrows() {
        User owner = userRepository.save(customer("duplicate-account-owner@example.com", "123456781"));

        accountRepository.saveAndFlush(account("NL12INHO0111111111", owner));

        Account duplicate = account("NL12INHO0111111111", owner);

        assertThrows(DataIntegrityViolationException.class, () -> {
            accountRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void accountDeletesByIban() {
        User owner = userRepository.save(customer("delete-account-owner@example.com", "123456782"));
        accountRepository.saveAndFlush(account("NL43INHO0222222222", owner));

        accountRepository.deleteByIban("NL43INHO0222222222");
        entityManager.flush();

        assertFalse(accountRepository.existsByIban("NL43INHO0222222222"));
    }

    private Account account(String iban, User owner) {
        Account account = new Account();
        account.setIban(iban);
        account.setType(AccountType.CHECKING);
        account.setBalance(BigDecimal.ZERO);
        account.setAbsoluteLimit(new BigDecimal("50.00"));
        account.setDailyLimit(new BigDecimal("100.00"));
        account.setActive(true);
        account.setCurrency(Currency.EURO);
        account.setOwner(owner);
        return account;
    }

    private User customer(String email, String bsn) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Customer");
        user.setEmail(email);
        user.setBsn(bsn);
        user.setPhoneNumber("+31612345670");
        user.setPasswordHash("hash");
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(true);
        return user;
    }
}
