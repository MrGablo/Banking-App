package com.example.demo.services;

import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.DuplicateException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.ApproveCustomerRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

//    private void requireEmployee() {
//        if (authContext.getCurrentUserRole() != UserRole.EMPLOYEE) {
//            throw new ForbiddenException("Employee role required");
//        }
//    }

}
