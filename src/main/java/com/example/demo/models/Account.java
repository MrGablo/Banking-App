package com.example.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    public String getIban() {
        return iban;
    }

    public AccountType getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }

    public double getAbsoluteLimit() {
        return absoluteLimit;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setAbsoluteLimit(double absoluteLimit) {
        this.absoluteLimit = absoluteLimit;
    }

    public void setDailyLimit(double dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

