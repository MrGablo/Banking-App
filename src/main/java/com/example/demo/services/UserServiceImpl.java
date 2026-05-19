package com.example.demo.services;

import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.CustomerIbanResponse;
import com.example.demo.dtos.UserResponse;
import com.example.demo.entity.Account;
import com.example.demo.common.enums.AccountType;
import com.example.demo.entity.User;
import com.example.demo.common.enums.UserRole;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.util.AccountUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountUtil accountUtil;

    public UserServiceImpl(UserRepository userRepository,
                           AccountRepository accountRepository, AccountUtil accountUtil){
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountUtil = accountUtil;
    }

    @Override
    public Page<UserResponse> getCustomersWithoutAccounts(Pageable pageable) {
        //requireEmployee();
        return userRepository.findCustomersWithoutAccounts(pageable)
                .map(UserResponse::from);
    }


    @Override
    public Page<UserResponse> getAllCustomers(Pageable pageable) {
        //requireEmployee();
        return userRepository.findByRole(UserRole.CUSTOMER, pageable)
                .map(UserResponse::from);
    }


    @Override
    @Transactional
    public UserResponse approveCustomer(Long userId, ApproveCustomerRequest request) {
        //requireEmployee();

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


        Account checking = accountUtil.newAccount(request.absoluteLimit(), request.dailyLimit(), AccountType.CHECKING);
        checking.setOwner(user);
        accountRepository.save(checking);


        Account savings = accountUtil.newAccount(request.absoluteLimit(), request.dailyLimit(), AccountType.SAVINGS);
        savings.setOwner(user);
        accountRepository.save(savings);

        return UserResponse.from(user);
    }

    @Override
    public List<CustomerIbanResponse> searchCustomerIbans(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        return userRepository
                .searchCustomers(
                        firstName.trim(),
                        lastName.trim(),
                        org.springframework.data.domain.PageRequest.of(0, 100)
                )
                .stream()
                .map(user -> new CustomerIbanResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        accountRepository.findByOwnerId(user.getId()).stream().map(Account::getIban).collect(Collectors.toList())
                ))
                .toList();
    }

//    private void requireEmployee() {
//        if (authContext.getCurrentUserRole() != UserRole.EMPLOYEE) {
//            throw new ForbiddenException("Employee role required");
//        }
//    }

}
