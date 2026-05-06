package com.example.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne
    private User owner;

}
