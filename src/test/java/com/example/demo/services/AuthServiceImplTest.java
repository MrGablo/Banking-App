package com.example.demo.services;

import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.DuplicateException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repositories.UserRepository;
import com.example.demo.util.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_whenEmailAlreadyExists_throwsDuplicateException() {
        RegisterRequest request = registerRequest();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateException.class, () -> authService.register(request));
    }

    @Test
    void register_whenUnique_savesMappedUser() {
        RegisterRequest request = registerRequest();
        User user = new User();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByBsn(request.bsn())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(request.phoneNumber())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);

        authService.register(request);

        verify(userRepository).save(user);
    }

    @Test
    void login_whenPasswordMatches_returnsTokenAndApprovedMessage() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        User user = user(true);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken("jane@example.com", java.util.Map.of("role", "CUSTOMER", "approved", true)))
                .thenReturn("jwt-token");

        var response = authService.login(request);

        assertEquals("Welcome back", response.message());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void login_whenPasswordDoesNotMatch_throwsUnauthorizedException() {
        LoginRequest request = new LoginRequest("jane@example.com", "wrong");
        User user = user(true);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_whenUserPendingApproval_returnsPendingMessage() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        User user = user(false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.eq("jane@example.com"), anyMap())).thenReturn("jwt-token");

        var response = authService.login(request);

        assertEquals("Welcome back - pending approval", response.message());
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

    private User user(boolean approved) {
        User user = new User();
        user.setEmail("jane@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(approved);
        return user;
    }
}
