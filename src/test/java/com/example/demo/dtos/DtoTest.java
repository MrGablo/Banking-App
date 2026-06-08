package com.example.demo.dtos;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.UserRole;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void registerRequest_validatesFields() {
        RegisterRequest request = new RegisterRequest(
                "", "", "not-an-email", "123", "abc", "123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolationFor(violations, "firstName"));
        assertTrue(hasViolationFor(violations, "lastName"));
        assertTrue(hasViolationFor(violations, "email"));
        assertTrue(hasViolationFor(violations, "bsn"));
        assertTrue(hasViolationFor(violations, "phoneNumber"));
        assertTrue(hasViolationFor(violations, "password"));
    }

    @Test
    void loginRequest_validatesFields() {
        LoginRequest request = new LoginRequest("invalid", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertTrue(hasViolationFor(violations, "email"));
        assertTrue(hasViolationFor(violations, "password"));
    }

    @Test
    void transferRequest_validatesFields() {
        TransferRequest request = new TransferRequest("", "", BigDecimal.ZERO, "Invalid transfer");

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        assertTrue(hasViolationFor(violations, "fromIban"));
        assertTrue(hasViolationFor(violations, "toIban"));
        assertTrue(hasViolationFor(violations, "amount"));
    }

    @Test
    void updateLimitsRequest_validatesDailyLimit() {
        UpdateLimitsRequest validRequest = new UpdateLimitsRequest(BigDecimal.ZERO, BigDecimal.ZERO);
        UpdateLimitsRequest invalidRequest = new UpdateLimitsRequest(BigDecimal.ZERO, new BigDecimal("-1.00"));

        assertTrue(validator.validate(validRequest).isEmpty());
        assertTrue(hasViolationFor(validator.validate(invalidRequest), "dailyLimit"));
    }

    @Test
    void approveCustomerRequest_requiresLimits() {
        ApproveCustomerRequest request = new ApproveCustomerRequest(null, null);

        Set<ConstraintViolation<ApproveCustomerRequest>> violations = validator.validate(request);

        assertTrue(hasViolationFor(violations, "absoluteLimit"));
        assertTrue(hasViolationFor(violations, "dailyLimit"));
    }

    @Test
    void accountResponseFrom_mapsFields() {
        User owner = user();
        Account account = new Account();
        account.setIban("NL01INHO0123456789");
        account.setType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("100.00"));
        account.setAbsoluteLimit(BigDecimal.ZERO);
        account.setDailyLimit(new BigDecimal("500.00"));
        account.setActive(true);
        account.setOwner(owner);

        AccountResponse response = AccountResponse.from(account);

        assertEquals("NL01INHO0123456789", response.iban());
        assertEquals(AccountType.CHECKING, response.type());
        assertEquals(new BigDecimal("100.00"), response.balance());
        assertEquals(1L, response.ownerId());
        assertEquals("Jane Doe", response.ownerName());
    }

    @Test
    void transactionResponseFrom_mapsFields() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction transaction = new Transaction();
        transaction.setId(10L);
        transaction.setFromIban("NL01INHO0111111111");
        transaction.setToIban("NL01INHO0222222222");
        transaction.setAmount(new BigDecimal("25.00"));
        transaction.setCreatedAt(timestamp);
        transaction.setUserInitiating("Jane");
        transaction.setDescription("Lunch");

        TransactionResponse response = TransactionResponse.from(transaction);

        assertEquals(10L, response.id());
        assertEquals("NL01INHO0111111111", response.fromIban());
        assertEquals("NL01INHO0222222222", response.toIban());
        assertEquals(new BigDecimal("25.00"), response.amount());
        assertEquals(timestamp, response.timestamp());
        assertEquals("Jane", response.userInitiating());
        assertEquals("Lunch", response.description());
    }

    @Test
    void records_exposeValues() {
        AuthResponse auth = new AuthResponse("Welcome back", true, "token");
        CustomerIbanResponse ibans = new CustomerIbanResponse(1L, "Jane", "Doe", List.of("NL01INHO0123456789"));
        UserDTO userDTO = new UserDTO(1L, "Jane", "Doe", "jane@test.com", "+31612345678",
                "CUSTOMER", true, true);

        assertEquals("Welcome back", auth.message());
        assertTrue(auth.approved());
        assertEquals("token", auth.token());
        assertEquals(List.of("NL01INHO0123456789"), ibans.ibans());
        assertEquals("CUSTOMER", userDTO.role());
    }

    @Test
    void messageResponse_returnsMessage() {
        MessageResponse response = new MessageResponse("Saved");

        assertEquals("Saved", response.getMessage());
    }

    private static boolean hasViolationFor(Set<? extends ConstraintViolation<?>> violations, String property) {
        return violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(property));
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@test.com");
        user.setBsn("123456789");
        user.setPhoneNumber("+31612345678");
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(true);
        user.setActive(true);
        return user;
    }
}
