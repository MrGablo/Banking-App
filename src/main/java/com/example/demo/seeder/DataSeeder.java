package com.example.demo.seeder;

import com.example.demo.models.Account;
import com.example.demo.models.AccountType;
import com.example.demo.models.Transaction;
import com.example.demo.models.User;
import com.example.demo.models.UserRole;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Profile("seed")
@Component
public class DataSeeder implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public DataSeeder(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User demoUser = new User("Jane", "Doe", "jane.doe@example.com", "123456789", "+31612345678", "password123", UserRole.CUSTOMER, true);
            userRepository.save(demoUser);

            Account a1 = new Account("NL01INHO0123456789", AccountType.CHECKING, 1250.50, -50.00, 500.00, true);
            a1.setOwner(demoUser);
            Account a2 = new Account("NL02INHO0987654321", AccountType.SAVINGS, 4200.00, 0.00, 0.00, true);
            a2.setOwner(demoUser);
            accountRepository.save(a1);
            accountRepository.save(a2);

            Transaction t1 = new Transaction(null, "NL01INHO0123456789", "NL02INHO0987654321", 75.00, LocalDateTime.now().minusDays(1), demoUser.getEmail());
            Transaction t2 = new Transaction(null, "NL02INHO0987654321", "NL01INHO0123456789", 25.00, LocalDateTime.now().minusHours(4), demoUser.getEmail());
            transactionRepository.save(t1);
            transactionRepository.save(t2);
        }
    }
}
