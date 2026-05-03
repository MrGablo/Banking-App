package com.example.demo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromIban;

    private String toIban;

    private double amount;

    private LocalDateTime timestamp;

    private String userInitiating;

    protected Transaction() {
        // JPA only
    }

    public Transaction(Long id, String fromIban, String toIban, double amount, LocalDateTime timestamp, String userInitiating) {
        this.id = id;
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
        this.timestamp = timestamp;
        this.userInitiating = userInitiating;
    }

}

