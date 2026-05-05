package com.example.demo.common.auth;

import com.example.demo.models.UserRole;

public interface AuthenticationContext {

    Long getCurrentUserId();

    UserRole getCurrentUserRole();
}
