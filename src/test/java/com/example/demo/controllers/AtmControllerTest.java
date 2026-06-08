package com.example.demo.controllers;

import com.example.demo.common.enums.TransferType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.User;
import com.example.demo.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AtmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TransactionService transactionService;

    private UsernamePasswordAuthenticationToken customerAuth;
    private UsernamePasswordAuthenticationToken employeeAuth;

    @BeforeEach
    void setUp() {
        User customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setApproved(true);

        customerAuth = new UsernamePasswordAuthenticationToken(
                customer, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        User employee = new User();
        employee.setId(2L);
        employee.setEmail("employee@test.com");
        employee.setRole(UserRole.EMPLOYEE);
        employee.setApproved(true);

        employeeAuth = new UsernamePasswordAuthenticationToken(
                employee, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    @Test
    void deposit_returnsCreated() throws Exception {
        TransferRequest request = new TransferRequest(
                null,
                "NL01INHO0123456789",
                new BigDecimal("20.00"),
                TransferType.ATM_DEPOSIT,
                "ATM deposit"
        );
        TransactionResponse response = new TransactionResponse(
                1L,
                null,
                "NL01INHO0123456789",
                new BigDecimal("20.00"),
                LocalDateTime.now(),
                "Jane Doe",
                "ATM deposit"
        );

        when(transactionService.atmDeposit(any(User.class), any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/atm/deposit")
                        .with(authentication(customerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.toIban").value("NL01INHO0123456789"))
                .andExpect(jsonPath("$.description").value("ATM deposit"));
    }

    @Test
    void withdraw_returnsCreated() throws Exception {
        TransferRequest request = new TransferRequest(
                "NL01INHO0123456789",
                null,
                new BigDecimal("10.00"),
                TransferType.ATM_WITHDRAWAL,
                "ATM withdrawal"
        );
        TransactionResponse response = new TransactionResponse(
                2L,
                "NL01INHO0123456789",
                null,
                new BigDecimal("10.00"),
                LocalDateTime.now(),
                "Jane Doe",
                "ATM withdrawal"
        );

        when(transactionService.atmWithdraw(any(User.class), any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/atm/withdraw")
                        .with(authentication(customerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromIban").value("NL01INHO0123456789"))
                .andExpect(jsonPath("$.description").value("ATM withdrawal"));
    }

    @Test
    void deposit_forbidsEmployee() throws Exception {
        TransferRequest request = new TransferRequest(
                null,
                "NL01INHO0123456789",
                new BigDecimal("20.00"),
                TransferType.ATM_DEPOSIT,
                "ATM deposit"
        );

        mockMvc.perform(post("/api/v1/atm/deposit")
                        .with(authentication(employeeAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void withdraw_forbidsAnonymous() throws Exception {
        TransferRequest request = new TransferRequest(
                "NL01INHO0123456789",
                null,
                new BigDecimal("10.00"),
                TransferType.ATM_WITHDRAWAL,
                "ATM withdrawal"
        );

        mockMvc.perform(post("/api/v1/atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
