package com.example.demo.domain.policy;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.exception.ConflictException;
import com.example.demo.common.exception.ForbiddenException;
import com.example.demo.common.exception.UnauthorizedException;
import com.example.demo.dtos.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class TransferPolicy {

    public void enforceAuthenticatedUser(User currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Not authenticated");
        }
    }

    public void enforceDifferentAccounts(TransferRequest request) {
        if (Objects.equals(request.fromIban(), request.toIban())) {
            throw new ConflictException("Source and destination accounts must be different");
        }
    }

    public void enforceApprovedUser(User currentUser) {
        if (!currentUser.isApproved()) {
            throw new ForbiddenException("User is not approved");
        }
    }

    public void enforceCheckingAccount(Account account) {
        if (account.getType() != AccountType.CHECKING) {
            throw new ConflictException("Only Checking Accounts Allowed");
        }
    }

    public void enforceSourceAccountOwnership(User currentUser, Account account) {
        if (account.getOwner() == null || !account.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only transfer from your own account");
        }
    }

    public void enforceAccountsBelongToUser(User currentUser, Account from, Account to) {
        if (from.getOwner() == null || to.getOwner() == null
                || !from.getOwner().getId().equals(currentUser.getId())
                || !to.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Accounts do not belong to user");
        }
    }

    public void enforcePersonalAccounts(Account from, Account to) {
        if (!isPersonalAccount(from.getType()) || !isPersonalAccount(to.getType())) {
            throw new ConflictException("Transfers are allowed only between checking and savings accounts");
        }
    }

    public BigDecimal enforceAbsoluteLimit(Account from, BigDecimal amount) {
        BigDecimal newBalance = from.getBalance().subtract(amount);
        BigDecimal minimumAllowedBalance = from.getAbsoluteLimit().negate();
        if (newBalance.compareTo(minimumAllowedBalance) < 0) {
            throw new ConflictException("Absolute limit exceeded");
        }
        return newBalance;
    }

    public void enforceDailyLimit(Account from, BigDecimal totalTransferredAmount, BigDecimal amount) {
        BigDecimal totalCurrentTransfer = totalTransferredAmount.add(amount);
        if (totalCurrentTransfer.compareTo(from.getDailyLimit()) > 0) {
            throw new ConflictException("Daily limit exceeded");
        }
    }

    private boolean isPersonalAccount(AccountType type) {
        return type == AccountType.CHECKING || type == AccountType.SAVINGS;
    }
}
