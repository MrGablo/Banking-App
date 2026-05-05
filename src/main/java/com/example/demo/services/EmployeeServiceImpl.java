package com.example.demo.services;

import com.example.demo.common.auth.AuthenticationContext;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.dtos.UserResponse;
import com.example.demo.models.Account;
import com.example.demo.models.AccountType;
import com.example.demo.models.User;
import com.example.demo.models.UserRole;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Random;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuthenticationContext authContext;

    public EmployeeServiceImpl(UserRepository userRepository,
                               AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               AuthenticationContext authContext) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.authContext = authContext;
    }

    public Page<UserResponse> getCustomersWithoutAccounts(Pageable pageable) {
        requireEmployee();
        return userRepository.findCustomersWithoutAccounts(pageable)
                .map(UserResponse::from);
    }


    public Page<UserResponse> getAllCustomers(Pageable pageable) {
        requireEmployee();
        return userRepository.findByRole(UserRole.CUSTOMER, pageable)
                .map(UserResponse::from);
    }


    @Transactional
    public UserResponse approveCustomer(Long userId, ApproveCustomerRequest request) {
        requireEmployee();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ConflictException("Only customers can be approved");
        }
        if (user.isApproved()) {
            throw new ConflictException("Customer is already approved");
        }

        user.setApproved(true);
        userRepository.save(user);


        Account checking = new Account(
                generateUniqueIban(),
                AccountType.CHECKING,
                0.0,
                request.absoluteLimit(),
                request.dailyLimit(),
                true
        );
        checking.setOwner(user);
        accountRepository.save(checking);


        Account savings = new Account(
                generateUniqueIban(),
                AccountType.SAVINGS,
                0.0,
                request.absoluteLimit(),
                request.dailyLimit(),
                true
        );
        savings.setOwner(user);
        accountRepository.save(savings);

        return UserResponse.from(user);
    }


    @Transactional
    public void closeAccount(String iban) {
        requireEmployee();

        Account account = accountRepository.findById(iban)
                .orElseThrow(() -> new NotFoundException("Account not found: " + iban));

        if (!account.isActive()) {
            throw new ConflictException("Account is already closed");
        }

        account.setActive(false);
        accountRepository.save(account);
    }


    @Transactional
    public void updateLimits(String iban, UpdateLimitsRequest request) {
        requireEmployee();

        Account account = accountRepository.findById(iban)
                .orElseThrow(() -> new NotFoundException("Account not found: " + iban));

        account.setAbsoluteLimit(request.absoluteLimit());
        account.setDailyLimit(request.dailyLimit());
        accountRepository.save(account);
    }


    private String generateUniqueIban() {
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


    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        requireEmployee();
        return accountRepository.findAll(pageable)
                .map(AccountResponse::from);
    }


    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        requireEmployee();
        return transactionRepository.findAll(pageable)
                .map(TransactionResponse::from);
    }


    public Page<TransactionResponse> getTransactionsForAccount(String iban, Pageable pageable) {
        requireEmployee();

        if (!accountRepository.existsByIban(iban)) {
            throw new NotFoundException("Account not found: " + iban);
        }

        return transactionRepository.findByFromIbanOrToIban(iban, iban, pageable)
                .map(TransactionResponse::from);
    }


    private void requireEmployee() {
        if (authContext.getCurrentUserRole() != UserRole.EMPLOYEE) {
            throw new ForbiddenException("Employee role required");
        }
    }
}
