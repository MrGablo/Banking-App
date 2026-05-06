package com.example.demo.mapper;

import com.example.demo.dtos.RegisterRequest;
import com.example.demo.models.User;
import com.example.demo.models.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        return new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.bsn(),
                request.phoneNumber(),
                request.password(),
                UserRole.CUSTOMER,
                false
        );
    }
}