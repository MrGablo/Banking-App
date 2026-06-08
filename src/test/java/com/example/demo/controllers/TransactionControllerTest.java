package com.example.demo.controllers;

import com.example.demo.common.enums.TransferType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.dtos.TransactionResponse;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TransactionService transactionService;

    private User employeeUser;
    private User customerUser;
    private UsernamePasswordAuthenticationToken employeeAuth;
    private UsernamePasswordAuthenticationToken customerAuth;

    @BeforeEach
    void setUp() {
        employeeUser = new User();
        employeeUser.setId(1L);
        employeeUser.setFirstName("John");
        employeeUser.setLastName("Doe");
        employeeUser.setEmail("john@test.com");
        employeeUser.setRole(UserRole.EMPLOYEE);
        employeeUser.setApproved(true);

        employeeAuth = new UsernamePasswordAuthenticationToken(
                employeeUser, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        customerUser = new User();
        customerUser.setId(2L);
        customerUser.setFirstName("Jane");
        customerUser.setLastName("Doe");
        customerUser.setEmail("jane@test.com");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setApproved(true);

        customerAuth = new UsernamePasswordAuthenticationToken(
                customerUser, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    //transferChecking
    @Test
    void transferCheckingReturnsCreated() throws Exception {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test transfer");

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setFromIban("NL01INHO0111111111");
        transaction.setToIban("NL01INHO0222222222");
        transaction.setAmount(new BigDecimal("100.00"));

        when(transactionService.transfer(any(TransferRequest.class), any(User.class), any(TransferType.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/v1/transactions/transfer-checking")
                        .with(authentication(employeeAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromIban").value("NL01INHO0111111111"));
    }

    @Test
    void transferCheckingForbiddenWithoutAuth() throws Exception {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("100.00"), "Test");

        mockMvc.perform(post("/api/v1/transactions/transfer-checking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    //transfer(own accounts)
    @Test
    void transferOwnAccountsReturnsCreated() throws Exception {
        TransferRequest request = new TransferRequest("NL01INHO0111111111", "NL01INHO0222222222",
                new BigDecimal("50.00"), "Own transfer");

        Transaction transaction = new Transaction();
        transaction.setId(2L);
        transaction.setFromIban("NL01INHO0111111111");
        transaction.setToIban("NL01INHO0222222222");
        transaction.setAmount(new BigDecimal("50.00"));

        when(transactionService.transfer(any(TransferRequest.class), any(User.class), any(TransferType.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .with(authentication(employeeAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    //getAllTransactions
    @Test
    void getAllTransactionsReturnsPage() throws Exception {
        TransactionResponse txResponse = new TransactionResponse(1L, "NL01INHO0111111111",
                "NL01INHO0222222222", new BigDecimal("100.00"), LocalDateTime.now(), "John", "Invoice");

        when(transactionService.getAllTransactions(any())).thenReturn(new PageImpl<>(List.of(txResponse)));

        mockMvc.perform(get("/api/v1/transactions")
                        .with(authentication(employeeAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromIban").value("NL01INHO0111111111"))
                .andExpect(jsonPath("$.content[0].description").value("Invoice"));
    }

    @Test
    void getAllTransactionsReturnsCurrentCustomerPage() throws Exception {
        TransactionResponse txResponse = new TransactionResponse(2L, "NL03INHO0111111111",
                "NL04INHO0222222222", new BigDecimal("25.00"), LocalDateTime.now(), "Jane", "Rent");

        when(transactionService.getTransactionsForUser(any(User.class), any())).thenReturn(new PageImpl<>(List.of(txResponse)));

        mockMvc.perform(get("/api/v1/transactions?page=0&size=8")
                        .with(authentication(customerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromIban").value("NL03INHO0111111111"))
                .andExpect(jsonPath("$.content[0].description").value("Rent"));
    }

    @Test
    void getAllTransactionsForbiddenWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isForbidden());
    }
}
