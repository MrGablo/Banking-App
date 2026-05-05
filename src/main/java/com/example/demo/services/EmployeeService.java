package com.example.demo.services;

import com.example.demo.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    public Page<UserResponse> getCustomersWithoutAccounts(Pageable pageable);

    public Page<UserResponse> getAllCustomers(Pageable pageable);

    public UserResponse approveCustomer(Long userId, ApproveCustomerRequest request);

    public void closeAccount(String iban);

    public void updateLimits(String iban, UpdateLimitsRequest request);

    public Page<AccountResponse> getAllAccounts(Pageable pageable);

    public Page<TransactionResponse> getAllTransactions(Pageable pageable);

    public Page<TransactionResponse> getTransactionsForAccount(String iban, Pageable pageable);
}
