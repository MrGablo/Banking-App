package com.example.demo.security;

import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final AccountRepository accountRepository;

    public CustomPermissionEvaluator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Not used in our project; evaluate by id instead
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User currentUser)) {
            throw new UnauthorizedException("Invalid principal");
        }

        if (isAdmin(authentication)) {
            return true;
        }

        if (!"account" .equalsIgnoreCase(targetType)) {
            return false;
        }

        String permissionName = permission == null ? "" : permission.toString().toLowerCase();
        if (!"view".equals(permissionName) && !"update".equals(permissionName) && !"delete".equals(permissionName) && !"manage".equals(permissionName)) {
            return false;
        }

        String idStr = targetId.toString();

        Optional<Account> accountOpt;
        // accept numeric id or IBAN string
        if (idStr.matches("\\d+")) {
            Long id = Long.valueOf(idStr);
            accountOpt = accountRepository.findById(id);
        } else {
            accountOpt = accountRepository.findByIban(idStr);
        }

        return accountOpt
                .map(Account::getOwner)
                .map(User::getId)
                .filter(ownerId -> ownerId.equals(currentUser.getId()))
                .isPresent();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch("ROLE_ADMIN"::equals);
    }
}

