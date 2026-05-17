package com.example.demo.util;

import com.example.demo.common.exception.ConflictException;
import com.example.demo.entity.Account;
import com.example.demo.common.enums.AccountType;
import com.example.demo.repositories.AccountRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

@Component
public class AccountUtil {

    private final AccountRepository accountRepository;

    public AccountUtil(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    public Account newAccount(BigDecimal absoluteLimit, BigDecimal dailyLimit, AccountType accountType){

        Account account = new Account();
        account.setIban(generateUniqueIban());
        account.setAbsoluteLimit(absoluteLimit);
        account.setDailyLimit(dailyLimit);
        account.setType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);

        return account;
    }
    public String generateUniqueIban() {
        for (int attempt = 0; attempt < 3; attempt++) {
            String iban = generateIban();
            if (!accountRepository.existsByIban(iban)) {
                return iban;
            }
        }
        throw new ConflictException("Failed to generate unique IBAN after 3 attempts");
    }

    private String generateIban() {
        Random random = new Random();
        // The account number is 9 - digit number with zeros preserved
        String accountNumber = String.format("%09d", random.nextInt(1_000_000_000));
        // The Bank code + account number
        String bban = "INHO0" + accountNumber;

        // I am moving "NL00" to the end, converting letters to digits, and computing check digits
        String rearranged = bban + "NL00";
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                numeric.append(c);
            }
        }
        BigInteger mod = new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97));
        int checkDigits = 98 - mod.intValue();

        return String.format("NL%02dINHO0%s", checkDigits, accountNumber);
    }
}
