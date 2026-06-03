package com.example.demo.controllers;

import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.dtos.UserDTO;
import com.example.demo.services.AuthService;
import com.example.demo.util.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_returnsCreated() {
        RegisterRequest request = registerRequest();

        var response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService).register(request);
    }

    @Test
    void login_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        AuthResponse authResponse = new AuthResponse("Welcome back", true, "jwt-token");
        when(authService.login(request)).thenReturn(authResponse);

        var response = authController.login(request);

        assertEquals(authResponse, response.getBody());
    }

    @Test
    void getCurrentUser_withBearerToken_returnsCurrentUser() {
        UserDTO user = new UserDTO(1L, "Jane", "Customer", "jane@example.com", "+31612345678", "CUSTOMER", true);
        when(jwtService.extractSubject("jwt-token")).thenReturn("jane@example.com");
        when(authService.getCurrentUser("jane@example.com")).thenReturn(user);

        var response = authController.getCurrentUser("Bearer jwt-token");

        assertEquals(user, response.getBody());
    }

    @Test
    void getCurrentUser_withoutBearerHeader_throwsUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> authController.getCurrentUser("jwt-token"));
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest(
                "Jane",
                "Customer",
                "jane@example.com",
                "123456789",
                "+31612345678",
                "password123"
        );
    }
}
