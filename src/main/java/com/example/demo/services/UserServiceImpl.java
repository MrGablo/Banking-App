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
import java.util.Map;
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
        return userRepository.findCustomersWithoutAccounts(pageable)
                .map(UserResponse::from);
    }


    @Override
    public Page<UserResponse> getAllCustomers(Pageable pageable) {
        return userRepository.findByRoleAndActive(UserRole.CUSTOMER, true, pageable)
                .map(UserResponse::from);
    }


    @Override
    @Transactional
    public UserResponse approveCustomer(Long userId, ApproveCustomerRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ConflictException("Only customers can be approved");
        }
        if (!user.isActive()) {
            throw new ConflictException("Customer is inactive");
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
    @Transactional
    public void deactivateCustomer(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ConflictException("Only customers can be deactivated");
        }
        if (!user.isActive()) {
            throw new ConflictException("Customer is already inactive");
        }

        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public List<CustomerIbanResponse> searchCustomerIbans(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        List<User> users = userRepository
                .searchUsersWithAccounts(
                        firstName.trim(),
                        lastName.trim(),
                        org.springframework.data.domain.PageRequest.of(0, 100)
                )
                .getContent();

        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();

        if (userIds.isEmpty()) {
            return List.of();
        }

        Map<Long, List<String>> ibansByOwnerId = accountRepository.findByOwnerIdIn(userIds)
                .stream()
                .collect(Collectors.groupingBy(
                        account -> account.getOwner().getId(),
                        Collectors.mapping(Account::getIban, Collectors.toList())
                ));

        return users.stream()
                .map(user -> new CustomerIbanResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        ibansByOwnerId.getOrDefault(user.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public CustomerIbanResponse searchCustomerByIban(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN is required");
        }

        Account account = accountRepository.findByIban(iban.trim())
                .orElseThrow(() -> new NotFoundException("Customer account not found for IBAN"));

        User owner = account.getOwner();
        if (owner == null || owner.getRole() != UserRole.CUSTOMER || !owner.isApproved() || !owner.isActive()) {
            throw new NotFoundException("Customer account not found for IBAN");
        }

        List<String> ibans = accountRepository.findByOwnerId(owner.getId())
                .stream()
                .map(Account::getIban)
                .toList();

        return new CustomerIbanResponse(
                owner.getId(),
                owner.getFirstName(),
                owner.getLastName(),
                ibans
        );
    }
}
