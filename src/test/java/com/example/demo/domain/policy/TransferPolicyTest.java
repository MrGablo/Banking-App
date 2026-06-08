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
    void validateTransfer_allowsOwner() {
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
    void validateTransfer_rejectsAnonymous() {
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
    void validateTransfer_rejectsSameAccount() {
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
    void validateTransfer_rejectsUnapprovedUser() {
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
    void validateTransfer_rejectsNonChecking() {
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
    void validateTransfer_rejectsNonOwner() {
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
    void validateTransfer_allowsEmployee() {
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
    void validateTransfer_allowsOwnAccounts() {
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
    void validateTransfer_rejectsOtherAccounts() {
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
    void validateTransfer_rejectsAbsoluteLimit() {
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
    void validateTransfer_rejectsDailyLimit() {
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

    @Test
    void validateAtmWithdrawal_allowsCustomerOwner() {
        User user = approvedCustomer(1L);
        Account account = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        TransferRequest request = new TransferRequest(
                "NL01INHO0111111111",
                null,
                new BigDecimal("40.00"),
                TransferType.ATM_WITHDRAWAL,
                "ATM withdrawal"
        );

        BigDecimal newBalance = transferPolicy.validateAtmWithdrawal(user, request, account, BigDecimal.ZERO);

        assertEquals(new BigDecimal("60.00"), newBalance);
    }

    @Test
    void validateAtmWithdrawal_rejectsEmployee() {
        User user = approvedUser(1L);
        Account account = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        TransferRequest request = new TransferRequest(
                "NL01INHO0111111111",
                null,
                new BigDecimal("40.00"),
                TransferType.ATM_WITHDRAWAL,
                "ATM withdrawal"
        );

        assertThrows(ForbiddenException.class,
                () -> transferPolicy.validateAtmWithdrawal(user, request, account, BigDecimal.ZERO));
    }

    @Test
    void validateAtmWithdrawal_rejectsMissingFromIban() {
        User user = approvedCustomer(1L);
        Account account = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        TransferRequest request = new TransferRequest(
                null,
                null,
                new BigDecimal("40.00"),
                TransferType.ATM_WITHDRAWAL,
                "ATM withdrawal"
        );

        assertThrows(ConflictException.class,
                () -> transferPolicy.validateAtmWithdrawal(user, request, account, BigDecimal.ZERO));
    }

    @Test
    void validateAtmDeposit_allowsCustomerOwner() {
        User user = approvedCustomer(1L);
        Account account = account(AccountType.CHECKING, user, "100.00", "50.00", "500.00");
        TransferRequest request = new TransferRequest(
                null,
                "NL01INHO0111111111",
                new BigDecimal("40.00"),
                TransferType.ATM_DEPOSIT,
                "ATM deposit"
        );

        assertDoesNotThrow(() -> transferPolicy.validateAtmDeposit(user, request, account));
    }

    @Test
    void validateAtmDeposit_rejectsOtherCustomerAccount() {
        User user = approvedCustomer(1L);
        Account account = account(AccountType.CHECKING, approvedCustomer(2L), "100.00", "50.00", "500.00");
        TransferRequest request = new TransferRequest(
                null,
                "NL01INHO0111111111",
                new BigDecimal("40.00"),
                TransferType.ATM_DEPOSIT,
                "ATM deposit"
        );

        assertThrows(ForbiddenException.class, () -> transferPolicy.validateAtmDeposit(user, request, account));
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
