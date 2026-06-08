package com.example.demo.services;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.CustomerIbanResponse;
import com.example.demo.dtos.UserResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUtil accountUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setEmail("jane@test.com");
        customer.setBsn("123456789");
        customer.setPhoneNumber("+31612345678");
        customer.setRole(UserRole.CUSTOMER);
        customer.setApproved(false);
        customer.setActive(true);
    }

    // --- getCustomersWithoutAccounts ---

    @Test
    void getCustomersWithoutAccountsReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(customer));
        when(userRepository.findCustomersWithoutAccounts(pageable)).thenReturn(page);

        Page<UserResponse> result = userService.getCustomersWithoutAccounts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Jane", result.getContent().get(0).firstName());
    }

    @Test
    void getCustomersWithoutAccountsReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of());
        when(userRepository.findCustomersWithoutAccounts(pageable)).thenReturn(page);

        Page<UserResponse> result = userService.getCustomersWithoutAccounts(pageable);

        assertEquals(0, result.getTotalElements());
    }

    // --- getAllCustomers ---

    @Test
    void getAllCustomersReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(customer));
        when(userRepository.findByRoleAndActive(UserRole.CUSTOMER, true, pageable)).thenReturn(page);

        Page<UserResponse> result = userService.getAllCustomers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Jane", result.getContent().get(0).firstName());
    }

    // --- approveCustomer ---

    @Test
    void approveCustomerSuccessfully() {
        ApproveCustomerRequest request = new ApproveCustomerRequest(new BigDecimal("0.00"), new BigDecimal("500.00"));

        Account checking = new Account();
        checking.setType(AccountType.CHECKING);
        Account savings = new Account();
        savings.setType(AccountType.SAVINGS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.save(customer)).thenReturn(customer);
        when(accountUtil.newAccount(request.absoluteLimit(), request.dailyLimit(), AccountType.CHECKING)).thenReturn(checking);
        when(accountUtil.newAccount(request.absoluteLimit(), request.dailyLimit(), AccountType.SAVINGS)).thenReturn(savings);

        UserResponse result = userService.approveCustomer(1L, request);

        assertTrue(customer.isApproved());
        assertEquals("Jane", result.firstName());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void approveCustomerThrowsWhenUserNotFound() {
        ApproveCustomerRequest request = new ApproveCustomerRequest(new BigDecimal("0.00"), new BigDecimal("500.00"));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.approveCustomer(99L, request));
    }

    @Test
    void approveCustomerThrowsWhenNotCustomerRole() {
        customer.setRole(UserRole.EMPLOYEE);
        ApproveCustomerRequest request = new ApproveCustomerRequest(new BigDecimal("0.00"), new BigDecimal("500.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(ConflictException.class, () -> userService.approveCustomer(1L, request));
    }

    @Test
    void approveCustomerThrowsWhenAlreadyApproved() {
        customer.setApproved(true);
        ApproveCustomerRequest request = new ApproveCustomerRequest(new BigDecimal("0.00"), new BigDecimal("500.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(ConflictException.class, () -> userService.approveCustomer(1L, request));
    }

    @Test
    void searchCustomerIbansReturnsOnlyCheckingAccounts() {
        customer.setApproved(true);
        Account checking = account("NL01INHO0123456789", AccountType.CHECKING);

        when(userRepository.searchUsersWithAccounts(eq("Jane"), eq("Doe"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(customer)));
        when(accountRepository.findByOwnerIdInAndType(List.of(1L), AccountType.CHECKING))
                .thenReturn(List.of(checking));

        List<CustomerIbanResponse> results = userService.searchCustomerIbans("Jane", "Doe");

        assertEquals(1, results.size());
        assertEquals(List.of("NL01INHO0123456789"), results.get(0).ibans());
    }

    @Test
    void searchCustomerByIbanReturnsOnlyOwnerCheckingAccounts() {
        customer.setApproved(true);
        Account searchedSavings = account("NL02INHO0987654321", AccountType.SAVINGS);
        Account checking = account("NL01INHO0123456789", AccountType.CHECKING);

        when(accountRepository.findByIban("NL02INHO0987654321")).thenReturn(Optional.of(searchedSavings));
        when(accountRepository.findByOwnerIdAndType(1L, AccountType.CHECKING)).thenReturn(List.of(checking));

        CustomerIbanResponse result = userService.searchCustomerByIban("NL02INHO0987654321");

        assertEquals(List.of("NL01INHO0123456789"), result.ibans());
    }

    private Account account(String iban, AccountType type) {
        Account account = new Account();
        account.setIban(iban);
        account.setType(type);
        account.setOwner(customer);
        return account;
    }
}
