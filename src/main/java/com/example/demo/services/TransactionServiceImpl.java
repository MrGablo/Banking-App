package com.example.demo.services;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;


    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository,
                                  UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Transaction> getTransactionById(long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public boolean deleteTransaction(long id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        //requireEmployee();
        return transactionRepository.findAll(pageable)
                .map(TransactionResponse::from);
    }


    @Override
    public Page<TransactionResponse> getTransactionsForAccount(String iban, Pageable pageable) {
        //requireEmployee();

        if (!accountRepository.existsByIban(iban)) {
            throw new NotFoundException("Account not found: " + iban);
        }

        return transactionRepository.findByFromIbanOrToIban(iban, iban, pageable)
                .map(TransactionResponse::from);
    }

    @Override
    public Transaction transferFromCheckingToChecking(TransferRequest request){

        if (Objects.equals(request.fromIban(), request.toIban())) {
            throw new ConflictException("Source and destination accounts must be different");
        }

        User user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isApproved()) {
            throw new ForbiddenException("User is not approved");
        }

        if(user.getRole() != UserRole.EMPLOYEE){
            throw new ForbiddenException("Only Employees are allowed");
        }

        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));

        if(from.getType() != AccountType.CHECKING){
            throw new ConflictException("Only Checking Accounts Allowed");
        }

        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new NotFoundException("To account not found"));


        if(to.getType() != AccountType.CHECKING){
            throw new ConflictException("Only Checking Accounts Allowed");
        }

        if(request.amount().compareTo(from.getBalance()) > 0){
            throw new ConflictException("Insufficient Funds");

        }

        //checking absolute Limit
        BigDecimal newBalance = from.getBalance().subtract(request.amount());
        if (newBalance.compareTo(from.getAbsoluteLimit()) < 0) {
            throw new ConflictException("Absolute limit exceeded");
        }

        //checking daily Limit
        BigDecimal totalTransferedAmount = transactionRepository.sumByFromIbanAndDate(
                from.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalCurrentTransfer = totalTransferedAmount.add(request.amount());

        if (totalCurrentTransfer.compareTo(from.getDailyLimit()) > 0){
            throw new ConflictException("Daily limit exceeded");
        }

        from.setBalance(newBalance);
        to.setBalance(to.getBalance().add(request.amount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = new Transaction();
        transaction.setFromIban(request.fromIban());
        transaction.setToIban(request.toIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(user.getFirstName());
        transaction.setType(AccountType.CHECKING);
        transaction.setCurrency(Currency.EURO);
        transaction.setDescription(request.description());

        return transactionRepository.save(transaction);

    }

    @Override
    @Transactional
    public Transaction transferBetweenOwnAccounts(TransferRequest request) {
        if (Objects.equals(request.fromIban(), request.toIban())) {
            throw new ConflictException("Source and destination accounts must be different");
        }

        User user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isApproved()) {
            throw new ForbiddenException("User is not approved");
        }

        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));
        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new NotFoundException("To account not found"));

        if (from.getOwner() == null || to.getOwner() == null || !from.getOwner().getId().equals(user.getId()) || !to.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("Accounts do not belong to user");
        }

        if (!isPersonalAccount(from.getType()) || !isPersonalAccount(to.getType())) {
            throw new ConflictException("Transfers are allowed only between checking and savings accounts");
        }

        BigDecimal newBalance = from.getBalance().subtract(request.amount());
        if (newBalance.compareTo(from.getAbsoluteLimit()) < 0) {
            throw new ConflictException("Absolute limit exceeded");
        }

        from.setBalance(newBalance);
        to.setBalance(to.getBalance().add(request.amount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = new Transaction();
        transaction.setFromIban(from.getIban());
        transaction.setToIban(to.getIban());
        transaction.setAmount(request.amount());
        transaction.setUserInitiating(user.getFirstName());
        transaction.setType(from.getType());
        transaction.setCurrency(from.getCurrency());
        transaction.setDescription(request.description());

        return transactionRepository.save(transaction);
    }

    private boolean isPersonalAccount(AccountType type) {
        return type == AccountType.CHECKING || type == AccountType.SAVINGS;
    }

//    private void requireEmployee() {
//        if (authContext.getCurrentUserRole() != UserRole.EMPLOYEE) {
//            throw new ForbiddenException("Employee role required");
//        }
//    }
}

