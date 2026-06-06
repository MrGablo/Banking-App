package com.example.demo.controllers;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.AccountResponse;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.UpdateLimitsRequest;
import com.example.demo.entity.User;
import com.example.demo.services.AccountService;
import com.example.demo.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private TransactionService transactionService;

    private UsernamePasswordAuthenticationToken employeeAuth;
    private UsernamePasswordAuthenticationToken customerAuth;

    @BeforeEach
    void setUp() {
        User employee = new User();
        employee.setId(1L);
        employee.setRole(UserRole.EMPLOYEE);
        employee.setEmail("employee@test.com");
        employeeAuth = new UsernamePasswordAuthenticationToken(
                employee, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        User customer = new User();
        customer.setId(2L);
        customer.setRole(UserRole.CUSTOMER);
        customer.setEmail("customer@test.com");
        customerAuth = new UsernamePasswordAuthenticationToken(
                customer, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    // --- closeAccount ---

    @Test
    void closeAccountReturnsOk() throws Exception {
        doNothing().when(accountService).closeAccount("NL01INHO0123456789");

        mockMvc.perform(post("/api/v1/accounts/NL01INHO0123456789/close")
                        .with(authentication(employeeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account successfully closed"));
    }

    @Test
    void closeAccountReturnsNotFound() throws Exception {
        doThrow(new NotFoundException("Account not found: NL01INHO0000000000"))
                .when(accountService).closeAccount("NL01INHO0000000000");

        mockMvc.perform(post("/api/v1/accounts/NL01INHO0000000000/close")
                        .with(authentication(employeeAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void closeAccountReturnsConflictWhenAlreadyClosed() throws Exception {
        doThrow(new ConflictException("Account is already closed"))
                .when(accountService).closeAccount("NL01INHO0123456789");

        mockMvc.perform(post("/api/v1/accounts/NL01INHO0123456789/close")
                        .with(authentication(employeeAuth)))
                .andExpect(status().isConflict());
    }

    @Test
    void closeAccountForbiddenForCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/NL01INHO0123456789/close")
                        .with(authentication(customerAuth)))
                .andExpect(status().isForbidden());
    }

    // --- updateLimits ---

    @Test
    void updateLimitsReturnsOk() throws Exception {
        UpdateLimitsRequest request = new UpdateLimitsRequest(new BigDecimal("100.00"), new BigDecimal("1000.00"));
        doNothing().when(accountService).updateLimits(eq("NL01INHO0123456789"), any());

        mockMvc.perform(put("/api/v1/accounts/NL01INHO0123456789/limits")
                        .with(authentication(employeeAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Limits successfully updated"));
    }

    @Test
    void updateLimitsForbiddenForCustomer() throws Exception {
        UpdateLimitsRequest request = new UpdateLimitsRequest(new BigDecimal("100.00"), new BigDecimal("1000.00"));

        mockMvc.perform(put("/api/v1/accounts/NL01INHO0123456789/limits")
                        .with(authentication(customerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- getAllAccounts ---

    @Test
    void getAllAccountsReturnsPage() throws Exception {
        AccountResponse response = new AccountResponse("NL01INHO0123456789", AccountType.CHECKING,
                new BigDecimal("1000.00"), new BigDecimal("0.00"), new BigDecimal("500.00"), true, 1L, "John Doe");

        when(accountService.getAllAccounts(any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/accounts")
                        .with(authentication(employeeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].iban").value("NL01INHO0123456789"));
    }

    @Test
    void getAllAccountsForbiddenWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isForbidden());
    }

    // --- getAccountTransactions ---

    @Test
    void getAccountTransactionsReturnsPage() throws Exception {
        TransactionResponse txResponse = new TransactionResponse(1L, "NL01INHO0111111111",
                "NL01INHO0222222222", new BigDecimal("100.00"), LocalDateTime.now(), "John");

        when(transactionService.getTransactionsForAccount(eq("NL01INHO0111111111"), any()))
                .thenReturn(new PageImpl<>(List.of(txResponse)));

        mockMvc.perform(get("/api/v1/accounts/NL01INHO0111111111/transactions")
                        .with(authentication(customerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromIban").value("NL01INHO0111111111"));
    }
}
