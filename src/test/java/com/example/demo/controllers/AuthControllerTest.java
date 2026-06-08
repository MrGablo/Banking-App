package com.example.demo.controllers;

import com.example.demo.common.exception.NotFoundException;
import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.dtos.UserDTO;
import com.example.demo.services.AuthService;
import com.example.demo.util.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void register_returnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "john@test.com", "123456789", "+31612345678", "password123");
        doNothing().when(authService).register(request);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User successfully registered"));
    }

    @Test
    void register_rejectsInvalidPayload() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "", "Doe", "not-an-email", "abc", "phone", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returnsAuthResponse() throws Exception {
        LoginRequest request = new LoginRequest("john@test.com", "password123");
        when(authService.login(request)).thenReturn(new AuthResponse("Welcome back", true, "jwt-token"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome back"))
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void getCurrentUser_returnsUser() throws Exception {
        UserDTO dto = new UserDTO(1L, "John", "Doe", "john@test.com", "+31612345678",
                "CUSTOMER", true, true);
        when(jwtService.extractSubject("jwt-token")).thenReturn("john@test.com");
        when(authService.getCurrentUser("john@test.com")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void getCurrentUser_rejectsBadHeader() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Token jwt-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_returnsNotFound() throws Exception {
        when(jwtService.extractSubject("jwt-token")).thenReturn("missing@test.com");
        when(authService.getCurrentUser("missing@test.com")).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isNotFound());
    }
}
