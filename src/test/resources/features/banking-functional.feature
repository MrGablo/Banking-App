Feature: Banking application functional flows

  Scenario: Employee approves a registered customer who can use the ATM
    Given an employee exists
    When a new customer registers through the API
    And the customer logs in
    Then the customer is told the account is pending approval
    When the employee logs in
    And the employee approves the registered customer with account limits
    Then the customer has a checking and savings account
    When the customer logs in
    And the customer deposits 50 euros at the ATM
    And the customer withdraws 20 euros at the ATM
    Then the customer transaction history contains ATM activity

  Scenario: Customer searches, transfers, hits a daily limit, and employee sees transactions
    Given an approved customer named "Jane" "Doe" exists with checking and savings accounts
    And an approved customer named "John" "Smith" exists with checking and savings accounts
    When the customer logs in
    Then the customer can search another customer by name
    And the customer can search another customer by IBAN
    When the customer transfers 25 euros to the other customer
    Then the transfer is recorded
    When the customer transfers 150 euros to the other customer
    Then the transfer is rejected because the daily limit is exceeded
    When the employee logs in
    Then the employee can view paginated transactions
