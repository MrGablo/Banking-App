package com.example.demo.services;

import com.example.demo.common.enums.TransferType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.domain.policy.TransferPolicy;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransferPolicy transferPolicy;
    private final TransactionMapper transactionMapper;


    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository,
                                  UserRepository userRepository, TransferPolicy transferPolicy, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transferPolicy = transferPolicy;
        this.transactionMapper = transactionMapper;
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
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        //requireEmployee();
        return transactionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(TransactionResponse::from);
    }

    @Override
    public Page<TransactionResponse> getTransactionsForUser(User currentUser, Pageable pageable) {
        List<String> ibans = accountRepository.findByOwnerId(currentUser.getId())
                .stream()
                .map(Account::getIban)
                .toList();

        if (ibans.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        return transactionRepository.findByFromIbanInOrToIbanInOrderByCreatedAtDesc(ibans, ibans, pageable)
                .map(TransactionResponse::from);
    }


    @Override
    public Page<TransactionResponse> getTransactionsForAccount(String iban, Pageable pageable) {

        if (!accountRepository.existsByIban(iban)) {
            throw new NotFoundException("Account not found: " + iban);
        }

        return transactionRepository.findByFromIbanOrToIban(iban, iban, pageable)
                .map(TransactionResponse::from);
    }

    @Override
    @Transactional
    public Transaction transfer(TransferRequest request, User currentUser, TransferType transferType) {

        User authenticatedUser = validateUser(currentUser);

        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));

        Account to = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new NotFoundException("To account not found"));

        validateUserAuthorization(authenticatedUser, from);

        BigDecimal totalTransferedAmount = calculateTotalTransfer(request);

        BigDecimal newBalance = transferPolicy.validateTransfer(authenticatedUser, request, from,
                to, totalTransferedAmount, transferType);

        from.setBalance(newBalance);
        to.setBalance(to.getBalance().add(request.amount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction transaction = transactionMapper.toEntity(request, authenticatedUser, transferType);

        return transactionRepository.save(transaction);

    }


    private BigDecimal calculateTotalTransfer(TransferRequest request) {
        Account from = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new NotFoundException("From account not found"));

        BigDecimal totalTransferedAmount = transactionRepository.sumByFromIbanAndDate(
                from.getIban(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).orElse(BigDecimal.ZERO);
        return totalTransferedAmount;
    }

    private User validateUser(User currentUser){
        return userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateUserAuthorization(User currentUser, Account from){
        if(currentUser.getRole() == UserRole.CUSTOMER
                && (from.getOwner() == null || !from.getOwner().getId().equals(currentUser.getId()))){
            throw new UnauthorizedException("Unauthorized to transfer from this account");
        }
    }
}

