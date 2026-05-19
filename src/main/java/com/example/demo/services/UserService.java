package com.example.demo.services;

import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    public Page<UserResponse> getCustomersWithoutAccounts(Pageable pageable);

    public Page<UserResponse> getAllCustomers(Pageable pageable);

    public UserResponse approveCustomer(Long userId, ApproveCustomerRequest request);
}
