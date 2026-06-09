package com.example.demo.config;

import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.TransferType;
import com.example.demo.entity.Account;
import com.example.demo.common.enums.AccountType;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.common.enums.UserRole;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Profile("seed")
@Component
public class DataSeeder implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {

            User demoUser = new User();
            demoUser.setFirstName("Jane");
            demoUser.setLastName("Doe");
            demoUser.setEmail("jane.doe@example.com");
            demoUser.setBsn("123456789");
            demoUser.setPhoneNumber("+31612345678");
            demoUser.setPasswordHash(passwordEncoder.encode("password123"));
            demoUser.setRole(UserRole.CUSTOMER);
            demoUser.setApproved(true);
            demoUser.setActive(true);
            userRepository.save(demoUser);

            User firstEmployeeUser = new User();
            firstEmployeeUser.setFirstName("Kosi");
            firstEmployeeUser.setLastName("MaryJane");
            firstEmployeeUser.setEmail("kosi.maryJane@example.com");
            firstEmployeeUser.setBsn("123456782");
            firstEmployeeUser.setPhoneNumber("+31612345633");
            firstEmployeeUser.setPasswordHash(passwordEncoder.encode("password123"));
            firstEmployeeUser.setRole(UserRole.EMPLOYEE);
            firstEmployeeUser.setApproved(true);
            firstEmployeeUser.setActive(true);
            userRepository.save(firstEmployeeUser);

            User employeeUser = new User();
            employeeUser.setFirstName("Henry");
            employeeUser.setLastName("Ekene");
            employeeUser.setEmail("henry.ekene@example.com");
            employeeUser.setBsn("123452389");
            employeeUser.setPhoneNumber("+31602345678");
            employeeUser.setPasswordHash(passwordEncoder.encode("password123"));
            employeeUser.setRole(UserRole.EMPLOYEE);
            employeeUser.setApproved(true);
            employeeUser.setActive(true);
            userRepository.save(employeeUser);

            // Employee-owned checking and savings accounts
            Account firstAccount = new Account();
            firstAccount.setIban("NL01INHO0123456789");
            firstAccount.setType(AccountType.CHECKING);
            firstAccount.setBalance(BigDecimal.valueOf(1250.50));
            firstAccount.setAbsoluteLimit(BigDecimal.valueOf(200));
            firstAccount.setDailyLimit(BigDecimal.valueOf(500.00));
            firstAccount.setActive(true);
            firstAccount.setCurrency(Currency.EURO);
            firstAccount.setOwner(firstEmployeeUser);

            Account secondAccount = new Account();
            secondAccount.setIban("NL02INHO0987654321");
            secondAccount.setType(AccountType.SAVINGS);
            secondAccount.setBalance(BigDecimal.valueOf(42000.00));
            secondAccount.setAbsoluteLimit(BigDecimal.valueOf(200));
            secondAccount.setDailyLimit(BigDecimal.valueOf(500.00));
            secondAccount.setActive(true);
            secondAccount.setCurrency(Currency.EURO);
            secondAccount.setOwner(firstEmployeeUser);

            // Demo (customer) account
            Account thirdAccount = new Account();
            thirdAccount.setIban("NL03INHO1234567890");
            thirdAccount.setType(AccountType.CHECKING);
            thirdAccount.setBalance(BigDecimal.valueOf(300.00));
            thirdAccount.setAbsoluteLimit(BigDecimal.valueOf(100));
            thirdAccount.setDailyLimit(BigDecimal.valueOf(200.00));
            thirdAccount.setActive(true);
            thirdAccount.setCurrency(Currency.EURO);
            thirdAccount.setOwner(demoUser);

            Account janeSavingsAccount = new Account();
            janeSavingsAccount.setIban("NL04INHO1122334455");
            janeSavingsAccount.setType(AccountType.SAVINGS);
            janeSavingsAccount.setBalance(BigDecimal.valueOf(15000.00));
            janeSavingsAccount.setAbsoluteLimit(BigDecimal.valueOf(500));
            janeSavingsAccount.setDailyLimit(BigDecimal.valueOf(1000.00));
            janeSavingsAccount.setActive(true);
            janeSavingsAccount.setCurrency(Currency.EURO);
            janeSavingsAccount.setOwner(demoUser);

            accountRepository.save(firstAccount);
            accountRepository.save(secondAccount);
            accountRepository.save(thirdAccount);
            accountRepository.save(janeSavingsAccount);

            Transaction firstTransaction = new Transaction();
            firstTransaction.setFromIban("NL01INHO0123456789");
            firstTransaction.setToIban("NL02INHO0987654321");
            firstTransaction.setAmount(BigDecimal.valueOf(75.00));
            firstTransaction.setUserInitiating("");
            firstTransaction.setTransferType(TransferType.CHECKING_TO_CHECKING);
            firstTransaction.setCurrency(Currency.EURO);
            firstTransaction.setDescription("");

            Transaction secondTransaction = new Transaction();
            secondTransaction.setFromIban("NL01INHO0123456789");
            secondTransaction.setToIban("NL02INHO0987654321");
            secondTransaction.setAmount(BigDecimal.valueOf(25.00));
            secondTransaction.setUserInitiating("");
            secondTransaction.setTransferType(TransferType.CHECKING_TO_CHECKING);
            secondTransaction.setCurrency(Currency.EURO);
            secondTransaction.setDescription("");

            transactionRepository.save(firstTransaction);
            transactionRepository.save(secondTransaction);


            
            for (int i = 1; i <= 18; i++) {
                Transaction customerTransaction = new Transaction();
                boolean outgoing = i % 2 == 0;
                customerTransaction.setFromIban(outgoing ? thirdAccount.getIban() : firstAccount.getIban());
                customerTransaction.setToIban(outgoing ? firstAccount.getIban() : thirdAccount.getIban());
                customerTransaction.setAmount(BigDecimal.valueOf(10L + i));
                customerTransaction.setUserInitiating(outgoing ? demoUser.getFirstName() : firstEmployeeUser.getFirstName());
                customerTransaction.setTransferType(TransferType.CHECKING_TO_CHECKING);
                customerTransaction.setCurrency(Currency.EURO);
                customerTransaction.setDescription("Seeded pagination transaction " + i);
                customerTransaction.setCreatedAt(LocalDateTime.now().minusDays(i));
                customerTransaction.setUpdatedAt(LocalDateTime.now().minusDays(i));
                transactionRepository.save(customerTransaction);
            }
        }
    }
}
