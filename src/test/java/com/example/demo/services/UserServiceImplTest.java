package com.example.demo.services;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.util.AccountUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void approveCustomerCreatesAccounts() {
        User user = customer();
        Account checking = new Account();
        Account savings = new Account();
        ApproveCustomerRequest request = new ApproveCustomerRequest(
                new BigDecimal("100.00"),
                new BigDecimal("500.00")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountUtil.newAccount(request.absoluteLimit(), request.dailyLimit(), AccountType.CHECKING))
                .thenReturn(checking);
        when(accountUtil.newAccount(request.absoluteLimit(), request.dailyLimit(), AccountType.SAVINGS))
                .thenReturn(savings);

        var response = userService.approveCustomer(1L, request);

        assertTrue(user.isApproved());
        assertEquals(1L, response.id());
        assertEquals(user, checking.getOwner());
        assertEquals(user, savings.getOwner());
        verify(accountRepository).save(checking);
        verify(accountRepository).save(savings);
        verify(userRepository).save(user);
    }

    @Test
    void missingCustomerThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.approveCustomer(1L, new ApproveCustomerRequest(BigDecimal.ZERO, BigDecimal.ZERO)));
    }

    @Test
    void approvedCustomerThrows() {
        User user = customer();
        user.setApproved(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class,
                () -> userService.approveCustomer(1L, new ApproveCustomerRequest(BigDecimal.ZERO, BigDecimal.ZERO)));
    }

    @Test
    void customerIbanSearchGroupsResults() {
        User user = customer();
        Account account = new Account();
        account.setIban("NL01INHO0123456789");
        account.setOwner(user);

        when(userRepository.searchUsersWithAccounts("Jane", "Customer", PageRequest.of(0, 100)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(accountRepository.findByOwnerIdIn(List.of(1L))).thenReturn(List.of(account));

        var results = userService.searchCustomerIbans(" Jane ", " Customer ");

        assertEquals(1, results.size());
        assertEquals(List.of("NL01INHO0123456789"), results.getFirst().ibans());
    }

    @Test
    void blankSearchThrows() {
        assertThrows(IllegalArgumentException.class, () -> userService.searchCustomerIbans("", "Customer"));
    }

    private User customer() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Jane");
        user.setLastName("Customer");
        user.setEmail("jane@example.com");
        user.setPhoneNumber("+31612345678");
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(false);
        return user;
    }
}
