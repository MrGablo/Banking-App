package com.example.demo.domain.policy;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.TransferType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferPolicyTest {

    private final TransferPolicy transferPolicy = new TransferPolicy();

    @Test
    void validateTransferAllowsCustomerCheckingToCheckingWhenTheyOwnSourceAccount() {
        User user = approvedCustomer(1L);
        Account from = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        Account to = account(AccountType.CHECKING, approvedUser(2L), "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        BigDecimal newBalance = transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.CHECKING_TO_CHECKING
        );

        assertEquals(new BigDecimal("60.00"), newBalance);
    }

    @Test
    void validateTransferRejectsUnauthenticatedUser() {
        Account from = account(AccountType.CHECKING, approvedUser(1L), "100.00", "50.00", "500.00");
        Account to = account(AccountType.CHECKING, approvedUser(2L), "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        assertThrows(UnauthorizedException.class, () -> transferPolicy.validateTransfer(
                null,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.CHECKING_TO_CHECKING
        ));
    }

    @Test
    void validateTransferRejectsSameSourceAndDestination() {
        User user = approvedUser(1L);
        Account account = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0111111111", "40.00");

        assertThrows(ConflictException.class, () -> transferPolicy.validateTransfer(
                user,
                request,
                account,
                account,
                BigDecimal.ZERO,
                TransferType.CHECKING_TO_CHECKING
        ));
    }

    @Test
    void validateTransferRejectsUnapprovedUser() {
        User user = user(1L, false);
        Account from = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        Account to = account(AccountType.CHECKING, approvedUser(2L), "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        assertThrows(ForbiddenException.class, () -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.CHECKING_TO_CHECKING
        ));
    }

    @Test
    void validateTransferRejectsNonCheckingAccountsForCheckingToCheckingTransfers() {
        User user = approvedUser(1L);
        Account from = account(AccountType.SAVINGS, user, "100.00", "50.00", "500.00");
        Account to = account(AccountType.CHECKING, approvedUser(2L), "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        assertThrows(ConflictException.class, () -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.CHECKING_TO_CHECKING
        ));
    }

    @Test
    void validateTransferRejectsCustomerCheckingToCheckingFromNonOwnerSourceAccount() {
        User user = approvedCustomer(1L);
        Account from = account(AccountType.CHECKING, approvedUser(2L), "100.00", "50.00", "500.00");
        Account to = account(AccountType.CHECKING, approvedUser(3L), "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        assertThrows(ForbiddenException.class, () -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.CHECKING_TO_CHECKING
        ));
    }

    @Test
    void validateTransferAllowsEmployeeCheckingToCheckingFromNonOwnerSourceAccount() {
        User user = approvedUser(1L);
        Account from = account(AccountType.CHECKING, approvedUser(2L), "100.00", "50.00", "500.00");
        Account to = account(AccountType.CHECKING, approvedUser(3L), "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        assertDoesNotThrow(() -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.CHECKING_TO_CHECKING
        ));
    }

    @Test
    void validateTransferAllowsCheckingAndSavingsOwnedByUser() {
        User user = approvedUser(1L);
        Account from = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        Account to = account(AccountType.SAVINGS, user, "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        assertDoesNotThrow(() -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.OWN_ACCOUNTS
        ));
    }

    @Test
    void validateTransferRejectsOwnAccountTransferWhenAccountsAreNotOwnedByUser() {
        User user = approvedUser(1L);
        Account from = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        Account to = account(AccountType.SAVINGS, approvedUser(2L), "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "40.00");

        assertThrows(ForbiddenException.class, () -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.OWN_ACCOUNTS
        ));
    }

    @Test
    void validateTransferRejectsAbsoluteLimitExceeded() {
        User user = approvedUser(1L);
        Account from = account(AccountType.CHECKING, user, "10.00", "5.00", "500.00");
        Account to = account(AccountType.SAVINGS, user, "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "20.00");

        assertThrows(ConflictException.class, () -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                BigDecimal.ZERO,
                TransferType.OWN_ACCOUNTS
        ));
    }

    @Test
    void validateTransferRejectsDailyLimitExceeded() {
        User user = approvedUser(1L);
        Account from = account(AccountType.CHECKING, user, "100.00", "50.00", "50.00");
        Account to = account(AccountType.SAVINGS, user, "25.00", "0.00", "500.00");
        TransferRequest request = request("NL01INHO0111111111", "NL01INHO0222222222", "20.00");

        assertThrows(ConflictException.class, () -> transferPolicy.validateTransfer(
                user,
                request,
                from,
                to,
                new BigDecimal("40.00"),
                TransferType.OWN_ACCOUNTS
        ));
    }

    private User approvedUser(Long id) {
        return user(id, true);
    }

    private User approvedCustomer(Long id) {
        User user = user(id, true);
        user.setRole(UserRole.CUSTOMER);
        return user;
    }

    private User user(Long id, boolean approved) {
        User user = new User();
        user.setId(id);
        user.setApproved(approved);
        user.setRole(UserRole.EMPLOYEE);
        return user;
    }

    private Account account(AccountType type, User owner, String balance, String absoluteLimit, String dailyLimit) {
        Account account = new Account();
        account.setType(type);
        account.setOwner(owner);
        account.setBalance(new BigDecimal(balance));
        account.setAbsoluteLimit(new BigDecimal(absoluteLimit));
        account.setDailyLimit(new BigDecimal(dailyLimit));
        return account;
    }

    private TransferRequest request(String fromIban, String toIban, String amount) {
        return new TransferRequest(fromIban, toIban, new BigDecimal(amount), "Test transfer");
    }
}
