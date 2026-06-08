package com.example.demo.controllers;

import com.example.demo.common.enums.UserRole;
import com.example.demo.dtos.CustomerIbanResponse;
import com.example.demo.entity.User;
import com.example.demo.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UsernamePasswordAuthenticationToken customerAuth;

    @BeforeEach
    void setUp() {
        User customer = new User();
        customer.setId(2L);
        customer.setRole(UserRole.CUSTOMER);
        customer.setEmail("customer@test.com");
        customerAuth = new UsernamePasswordAuthenticationToken(
                customer, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void searchCustomerByIbanReturnsCustomerIbans() throws Exception {
        CustomerIbanResponse response = new CustomerIbanResponse(
                2L,
                "Jane",
                "Doe",
                List.of("NL03INHO1234567890", "NL04INHO1234567890")
        );

        when(userService.searchCustomerByIban("NL03INHO1234567890")).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/customer-ibans/search-by-iban")
                        .param("iban", "NL03INHO1234567890")
                        .with(authentication(customerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.ibans[0]").value("NL03INHO1234567890"));
    }
}
