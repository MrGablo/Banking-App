package com.example.demo.mapper;

import com.example.demo.common.enums.UserRole;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserMapperTest {

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserMapper userMapper = new UserMapper(passwordEncoder);

    @Test
    void toEntity_mapsRegisterRequest() {
        RegisterRequest request = new RegisterRequest(
                "Jane", "Doe", "jane@test.com", "123456789", "+31612345678", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        User user = userMapper.toEntity(request);

        assertEquals("Jane", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("jane@test.com", user.getEmail());
        assertEquals("123456789", user.getBsn());
        assertEquals("+31612345678", user.getPhoneNumber());
        assertEquals(UserRole.CUSTOMER, user.getRole());
        assertEquals("hashed-password", user.getPasswordHash());
        assertFalse(user.isApproved());
        assertTrue(user.isActive());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void toEntity_returnsNull() {
        assertNull(userMapper.toEntity(null));
    }

    @Test
    void toDTO_mapsFields() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@test.com");
        user.setPhoneNumber("+31612345678");
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(true);
        user.setActive(true);

        UserDTO dto = userMapper.toDTO(user);

        assertEquals(1L, dto.id());
        assertEquals("Jane", dto.firstName());
        assertEquals("Doe", dto.lastName());
        assertEquals("jane@test.com", dto.email());
        assertEquals("+31612345678", dto.phoneNumber());
        assertEquals("CUSTOMER", dto.role());
        assertTrue(dto.approved());
        assertTrue(dto.active());
    }

    @Test
    void toDTO_returnsNull() {
        assertNull(userMapper.toDTO(null));
    }
}
