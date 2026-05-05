package com.example.demo.common.auth;

import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.models.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class HeaderAuthenticationContext implements AuthenticationContext {

    private final HttpServletRequest request;

    public HeaderAuthenticationContext(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Long getCurrentUserId() {
        String header = request.getHeader("X-User-Id");
        if (header == null) {
            throw new ForbiddenException("Not authenticated");
        }
        return Long.parseLong(header);
    }

    @Override
    public UserRole getCurrentUserRole() {
        String header = request.getHeader("X-User-Role");
        if (header == null) {
            throw new ForbiddenException("Not authenticated");
        }
        return UserRole.valueOf(header);
    }
}
