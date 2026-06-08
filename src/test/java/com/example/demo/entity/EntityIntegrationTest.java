package com.example.demo.entity;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.TransferType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveUser_persistsFields() {
        User user = validUser("jane@test.com", "123456789", "+31612345678");

        User saved = userRepository.save(user);
        entityManager.flush();
        Optional<User> loaded = userRepository.findById(saved.getId());

        assertTrue(loaded.isPresent());
        assertNotNull(saved.getId());
        assertEquals("Jane", loaded.get().getFirstName());
        assertEquals("Doe", loaded.get().getLastName());
        assertEquals(UserRole.CUSTOMER, loaded.get().getRole());
        assertTrue(loaded.get().isActive());
        assertFalse(loaded.get().isApproved());
        assertNotNull(loaded.get().getCreatedAt());
        assertNotNull(loaded.get().getUpdatedAt());
    }

    @Test
    void saveUser_throwsForDuplicateEmail() {
        userRepository.save(validUser("duplicate@test.com", "111111111", "+31611111111"));
        entityManager.flush();

        User duplicate = validUser("duplicate@test.com", "222222222", "+31622222222");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void saveAccount_persistsFields() {
        User owner = userRepository.save(validUser("owner@test.com", "333333333", "+31633333333"));
        Account account = validAccount("NL01INHO0123456789", owner);

        Account saved = accountRepository.save(account);
        entityManager.flush();
        entityManager.clear();
        Optional<Account> loaded = accountRepository.findById(saved.getId());

        assertTrue(loaded.isPresent());
        assertNotNull(saved.getId());
        assertEquals("NL01INHO0123456789", loaded.get().getIban());
        assertEquals(AccountType.CHECKING, loaded.get().getType());
        assertEquals(Currency.EURO, loaded.get().getCurrency());
        assertEquals(new BigDecimal("250.00"), loaded.get().getBalance());
        assertEquals(new BigDecimal("0.00"), loaded.get().getAbsoluteLimit());
        assertEquals(new BigDecimal("500.00"), loaded.get().getDailyLimit());
        assertTrue(loaded.get().isActive());
        assertEquals(owner.getId(), loaded.get().getOwner().getId());
        assertNotNull(loaded.get().getCreatedAt());
        assertNotNull(loaded.get().getUpdatedAt());
    }

    @Test
    void saveAccount_throwsForDuplicateIban() {
        User owner = userRepository.save(validUser("iban-owner@test.com", "444444444", "+31644444444"));
        accountRepository.save(validAccount("NL02INHO0123456789", owner));
        entityManager.flush();

        Account duplicate = validAccount("NL02INHO0123456789", owner);

        assertThrows(DataIntegrityViolationException.class, () -> {
            accountRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void saveTransaction_persistsFields() {
        Transaction transaction = new Transaction();
        transaction.setFromIban("NL01INHO0111111111");
        transaction.setToIban("NL01INHO0222222222");
        transaction.setAmount(new BigDecimal("75.00"));
        transaction.setUserInitiating("Jane");
        transaction.setCurrency(Currency.EURO);
        transaction.setTransferType(TransferType.CHECKING_TO_CHECKING);
        transaction.setDescription("Invoice");

        Transaction saved = transactionRepository.save(transaction);
        entityManager.flush();
        Optional<Transaction> loaded = transactionRepository.findById(saved.getId());

        assertTrue(loaded.isPresent());
        assertNotNull(saved.getId());
        assertEquals("NL01INHO0111111111", loaded.get().getFromIban());
        assertEquals("NL01INHO0222222222", loaded.get().getToIban());
        assertEquals(new BigDecimal("75.00"), loaded.get().getAmount());
        assertEquals("Jane", loaded.get().getUserInitiating());
        assertEquals(Currency.EURO, loaded.get().getCurrency());
        assertEquals(TransferType.CHECKING_TO_CHECKING, loaded.get().getTransferType());
        assertEquals("Invoice", loaded.get().getDescription());
        assertNotNull(loaded.get().getCreatedAt());
        assertNotNull(loaded.get().getUpdatedAt());
    }

    private User validUser(String email, String bsn, String phoneNumber) {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail(email);
        user.setBsn(bsn);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash("hashed-password");
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(false);
        user.setActive(true);
        return user;
    }

    private Account validAccount(String iban, User owner) {
        Account account = new Account();
        account.setIban(iban);
        account.setType(AccountType.CHECKING);
        account.setCurrency(Currency.EURO);
        account.setBalance(new BigDecimal("250.00"));
        account.setAbsoluteLimit(new BigDecimal("0.00"));
        account.setDailyLimit(new BigDecimal("500.00"));
        account.setActive(true);
        account.setOwner(owner);
        return account;
    }
}
