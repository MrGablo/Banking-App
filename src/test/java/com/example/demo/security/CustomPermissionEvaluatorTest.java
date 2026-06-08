package com.example.demo.security;

import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomPermissionEvaluatorTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final CustomPermissionEvaluator evaluator = new CustomPermissionEvaluator(accountRepository);

    private User currentUser;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        authentication = new UsernamePasswordAuthenticationToken(currentUser, null, List.of());
    }

    @Test
    void hasPermission_allowsOwnerByIban() {
        Account account = accountOwnedBy(currentUser);
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(account));

        boolean result = evaluator.hasPermission(authentication, "NL01INHO0123456789", "account", "view");

        assertTrue(result);
    }

    @Test
    void hasPermission_allowsOwnerById() {
        Account account = accountOwnedBy(currentUser);
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        boolean result = evaluator.hasPermission(authentication, 10L, "account", "manage");

        assertTrue(result);
    }

    @Test
    void hasPermission_rejectsNonOwner() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(accountRepository.findByIban("NL01INHO0123456789")).thenReturn(Optional.of(accountOwnedBy(otherUser)));

        boolean result = evaluator.hasPermission(authentication, "NL01INHO0123456789", "account", "view");

        assertFalse(result);
    }

    @Test
    void hasPermission_rejectsInvalidInput() {
        assertFalse(evaluator.hasPermission(null, "NL01INHO0123456789", "account", "view"));
        assertFalse(evaluator.hasPermission(authentication, "NL01INHO0123456789", "user", "view"));
        assertFalse(evaluator.hasPermission(authentication, "NL01INHO0123456789", "account", "delete"));
        assertFalse(evaluator.hasPermission(authentication, new Object(), "view"));
    }

    private Account accountOwnedBy(User owner) {
        Account account = new Account();
        account.setOwner(owner);
        return account;
    }
}
