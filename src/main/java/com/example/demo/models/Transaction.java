package com.example.demo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromIban;

    private String toIban;

    private double amount;

    private LocalDateTime timestamp;

    private String userInitiating;



    public Transaction(Long id, String fromIban, String toIban, double amount, LocalDateTime timestamp, String userInitiating) {
        this.id = id;
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
        this.timestamp = timestamp;
        this.userInitiating = userInitiating;
    }

}

