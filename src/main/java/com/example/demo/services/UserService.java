package com.example.demo.services;

import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.CustomerIbanResponse;
import com.example.demo.dtos.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    public Page<UserResponse> getCustomersWithoutAccounts(Pageable pageable);

    public Page<UserResponse> getAllCustomers(Pageable pageable);

    public UserResponse approveCustomer(Long userId, ApproveCustomerRequest request);

    public void deactivateCustomer(Long userId);

    List<CustomerIbanResponse> searchCustomerIbans(String firstName, String lastName);
}
