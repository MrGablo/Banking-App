package com.example.demo.services;

import com.example.demo.common.exception.DuplicateException;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    private RegisterRequest validRequest() {
        return new RegisterRequest("John", "Doe", "john@test.com", "123456789", "+31612345678", "password123");
    }

    @Test
    void registerSuccessfully() {
        RegisterRequest request = validRequest();
        User user = new User();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByBsn(request.bsn())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(request.phoneNumber())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);

        assertDoesNotThrow(() -> authService.register(request));
        verify(userRepository).save(user);
    }

    @Test
    void registerThrowsWhenEmailExists() {
        RegisterRequest request = validRequest();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerThrowsWhenBsnExists() {
        RegisterRequest request = validRequest();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByBsn(request.bsn())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerThrowsWhenPhoneNumberExists() {
        RegisterRequest request = validRequest();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByBsn(request.bsn())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(request.phoneNumber())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
