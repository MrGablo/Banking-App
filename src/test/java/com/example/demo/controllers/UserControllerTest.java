package com.example.demo.controllers;

import com.example.demo.common.enums.UserRole;
import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.CustomerIbanResponse;
import com.example.demo.dtos.UserResponse;
import com.example.demo.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getCustomersWithoutAccounts_capsPageSizeAtOneHundred() {
        UserResponse user = userResponse();
        when(userService.getCustomersWithoutAccounts(PageRequest.of(0, 100)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 100), 1));

        var response = userController.getCustomersWithoutAccounts(0, 500);

        assertEquals(1, response.totalElements());
        assertEquals(100, response.size());
    }

    @Test
    void searchCustomerIbans_returnsMatchingCustomers() {
        CustomerIbanResponse result = new CustomerIbanResponse(
                1L,
                "Jane",
                "Customer",
                List.of("NL01INHO0123456789")
        );
        when(userService.searchCustomerIbans("Jane", "Customer")).thenReturn(List.of(result));

        var response = userController.searchCustomerIbans("Jane", "Customer");

        assertEquals(List.of(result), response.getBody());
    }

    @Test
    void approveCustomer_returnsCreatedUserResponse() {
        ApproveCustomerRequest request = new ApproveCustomerRequest(
                new BigDecimal("100.00"),
                new BigDecimal("500.00")
        );
        UserResponse user = userResponse();
        when(userService.approveCustomer(1L, request)).thenReturn(user);

        var response = userController.approveCustomer(1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(userService).approveCustomer(1L, request);
    }

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "Jane",
                "Customer",
                "jane@example.com",
                "123456789",
                "+31612345678",
                UserRole.CUSTOMER,
                true
        );
    }
}
