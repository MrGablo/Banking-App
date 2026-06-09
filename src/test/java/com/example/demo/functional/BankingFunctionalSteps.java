package com.example.demo.functional;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.UserRole;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BankingFunctionalSteps {
    private static final String PASSWORD = "password123";
    private static final String EMPLOYEE_EMAIL = "employee.functional@example.com";
    private static final String CUSTOMER_EMAIL = "customer.functional@example.com";
    private static final String OTHER_CUSTOMER_EMAIL = "other.functional@example.com";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String employeeToken;
    private String customerToken;
    private String customerCheckingIban;
    private String customerSavingsIban;
    private String otherCustomerCheckingIban;
    private MvcResult lastResult;

    @Before
    public void cleanDatabase() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        employeeToken = null;
        customerToken = null;
        customerCheckingIban = null;
        customerSavingsIban = null;
        otherCustomerCheckingIban = null;
        lastResult = null;
    }

    @Given("an employee exists")
    public void anEmployeeExists() {
        createUser("Functional", "Employee", EMPLOYEE_EMAIL, "800000001", "+31680000001", UserRole.EMPLOYEE, true);
    }

    @When("a new customer registers through the API")
    public void aNewCustomerRegistersThroughTheApi() throws Exception {
        lastResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", "Functional",
                                "lastName", "Customer",
                                "email", CUSTOMER_EMAIL,
                                "bsn", "800000002",
                                "phoneNumber", "+31680000002",
                                "password", PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @When("the customer logs in")
    public void theCustomerLogsIn() throws Exception {
        lastResult = login(CUSTOMER_EMAIL);
        customerToken = jsonNode(lastResult).get("token").asText();
    }

    @Then("the customer is told the account is pending approval")
    public void theCustomerIsToldTheAccountIsPendingApproval() throws Exception {
        JsonNode response = jsonNode(lastResult);
        assertThat(response.get("approved").asBoolean()).isFalse();
        assertThat(response.get("message").asText()).containsIgnoringCase("pending");
    }

    @When("the employee logs in")
    public void theEmployeeLogsIn() throws Exception {
        if (userRepository.findByEmail(EMPLOYEE_EMAIL).isEmpty()) {
            anEmployeeExists();
        }
        lastResult = login(EMPLOYEE_EMAIL);
        employeeToken = jsonNode(lastResult).get("token").asText();
    }

    @And("the employee approves the registered customer with account limits")
    public void theEmployeeApprovesTheRegisteredCustomerWithAccountLimits() throws Exception {
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        lastResult = mockMvc.perform(post("/api/v1/users/{userId}/approve", customer.getId())
                        .header("Authorization", bearer(employeeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "absoluteLimit", 0,
                                "dailyLimit", 200
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Then("the customer has a checking and savings account")
    public void theCustomerHasACheckingAndSavingsAccount() {
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        List<Account> accounts = accountRepository.findByOwnerId(customer.getId());

        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(Account::getType).contains(AccountType.CHECKING, AccountType.SAVINGS);

        customerCheckingIban = accounts.stream()
                .filter(account -> account.getType() == AccountType.CHECKING)
                .findFirst()
                .orElseThrow()
                .getIban();
        customerSavingsIban = accounts.stream()
                .filter(account -> account.getType() == AccountType.SAVINGS)
                .findFirst()
                .orElseThrow()
                .getIban();
    }

    @And("the customer deposits {int} euros at the ATM")
    public void theCustomerDepositsEurosAtTheAtm(int amount) throws Exception {
        lastResult = mockMvc.perform(post("/api/v1/atm/deposit")
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "toIban", customerCheckingIban,
                                "amount", amount,
                                "transferType", "ATM_DEPOSIT",
                                "description", "Functional ATM deposit"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @And("the customer withdraws {int} euros at the ATM")
    public void theCustomerWithdrawsEurosAtTheAtm(int amount) throws Exception {
        lastResult = mockMvc.perform(post("/api/v1/atm/withdraw")
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fromIban", customerCheckingIban,
                                "amount", amount,
                                "transferType", "ATM_WITHDRAWAL",
                                "description", "Functional ATM withdrawal"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Then("the customer transaction history contains ATM activity")
    public void theCustomerTransactionHistoryContainsAtmActivity() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/transactions?page=0&size=10")
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = jsonNode(result).get("content");
        assertThat(content).isNotNull();
        assertThat(content.size()).isGreaterThanOrEqualTo(2);
        assertThat(content.toString()).contains("Functional ATM deposit", "Functional ATM withdrawal");
    }

    @Given("an approved customer named {string} {string} exists with checking and savings accounts")
    public void anApprovedCustomerNamedExistsWithCheckingAndSavingsAccounts(String firstName, String lastName) {
        String email = firstName.equals("Jane") ? CUSTOMER_EMAIL : OTHER_CUSTOMER_EMAIL;
        String bsn = firstName.equals("Jane") ? "800000003" : "800000004";
        String phone = firstName.equals("Jane") ? "+31680000003" : "+31680000004";
        User customer = createUser(firstName, lastName, email, bsn, phone, UserRole.CUSTOMER, true);

        Account checking = createAccount(
                firstName.equals("Jane") ? "NL01INHO0000000001" : "NL01INHO0000000003",
                AccountType.CHECKING,
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                customer
        );
        Account savings = createAccount(
                firstName.equals("Jane") ? "NL01INHO0000000002" : "NL01INHO0000000004",
                AccountType.SAVINGS,
                new BigDecimal("150.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                customer
        );

        if (firstName.equals("Jane")) {
            customerCheckingIban = checking.getIban();
            customerSavingsIban = savings.getIban();
        } else {
            otherCustomerCheckingIban = checking.getIban();
        }
    }

    @Then("the customer can search another customer by name")
    public void theCustomerCanSearchAnotherCustomerByName() throws Exception {
        lastResult = mockMvc.perform(get("/api/v1/users/customer-ibans")
                        .header("Authorization", bearer(customerToken))
                        .param("firstName", "John")
                        .param("lastName", "Smith"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(lastResult.getResponse().getContentAsString()).contains(otherCustomerCheckingIban);
    }

    @And("the customer can search another customer by IBAN")
    public void theCustomerCanSearchAnotherCustomerByIban() throws Exception {
        lastResult = mockMvc.perform(get("/api/v1/users/customer-ibans/search-by-iban")
                        .header("Authorization", bearer(customerToken))
                        .param("iban", otherCustomerCheckingIban))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(lastResult.getResponse().getContentAsString()).contains("John", "Smith", otherCustomerCheckingIban);
    }

    @When("the customer transfers {int} euros to the other customer")
    public void theCustomerTransfersEurosToTheOtherCustomer(int amount) throws Exception {
        lastResult = mockMvc.perform(post("/api/v1/transactions/transfer-checking")
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fromIban", customerCheckingIban,
                                "toIban", otherCustomerCheckingIban,
                                "amount", amount,
                                "transferType", "CHECKING_TO_CHECKING",
                                "description", "Functional customer transfer"
                        ))))
                .andReturn();
    }

    @Then("the transfer is recorded")
    public void theTransferIsRecorded() throws Exception {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(201);
        assertThat(lastResult.getResponse().getContentAsString()).contains(customerCheckingIban, otherCustomerCheckingIban);
    }

    @Then("the transfer is rejected because the daily limit is exceeded")
    public void theTransferIsRejectedBecauseTheDailyLimitIsExceeded() throws Exception {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(409);
        assertThat(lastResult.getResponse().getContentAsString()).contains("Daily limit exceeded");
    }

    @Then("the employee can view paginated transactions")
    public void theEmployeeCanViewPaginatedTransactions() throws Exception {
        lastResult = mockMvc.perform(get("/api/v1/transactions?page=0&size=1")
                        .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = jsonNode(lastResult);
        assertThat(response.get("content").size()).isEqualTo(1);
        assertThat(response.get("totalElements").asInt()).isGreaterThanOrEqualTo(1);
    }

    private MvcResult login(String email) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private User createUser(String firstName, String lastName, String email, String bsn, String phoneNumber,
                            UserRole role, boolean approved) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBsn(bsn);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setApproved(approved);
        user.setActive(true);
        return userRepository.save(user);
    }

    private Account createAccount(String iban, AccountType type, BigDecimal balance, BigDecimal absoluteLimit,
                                  BigDecimal dailyLimit, User owner) {
        Account account = new Account();
        account.setIban(iban);
        account.setType(type);
        account.setBalance(balance);
        account.setAbsoluteLimit(absoluteLimit);
        account.setDailyLimit(dailyLimit);
        account.setActive(true);
        account.setCurrency(Currency.EURO);
        account.setOwner(owner);
        return accountRepository.save(account);
    }

    private JsonNode jsonNode(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
