package com.example.demo.seeder;

import com.example.demo.models.Account;
import com.example.demo.models.AccountType;
import com.example.demo.models.Transaction;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DataSeeder(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            Account a1 = new Account("NL01INHO0123456789", AccountType.CHECKING, 1250.50, -50.00, 500.00, true);
            Account a2 = new Account("NL02INHO0987654321", AccountType.SAVINGS, 4200.00, 0.00, 0.00, true);
            accountRepository.save(a1);
            accountRepository.save(a2);

            Transaction t1 = new Transaction(null, "NL01INHO0123456789", "NL02INHO0987654321", 75.00, LocalDateTime.now().minusDays(1), "j.doe@example.com");
            Transaction t2 = new Transaction(null, "NL02INHO0987654321", "NL01INHO0123456789", 25.00, LocalDateTime.now().minusHours(4), "j.doe@example.com");
            transactionRepository.save(t1);
            transactionRepository.save(t2);
        }
    }
}


