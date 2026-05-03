package com.example.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(length = 34)
    private String iban;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private double balance;

    private double absoluteLimit;

    private double dailyLimit;

    private boolean active;

    protected Account() {
        // JPA only
    }

    public Account(String iban, AccountType type, double balance, double absoluteLimit, double dailyLimit, boolean active) {
        this.iban = iban;
        this.type = type;
        this.balance = balance;
        this.absoluteLimit = absoluteLimit;
        this.dailyLimit = dailyLimit;
        this.active = active;
    }

}

