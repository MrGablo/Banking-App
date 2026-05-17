package com.example.demo.entity;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromIban;

    private String toIban;

    private BigDecimal amount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String userInitiating;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private String description;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = LocalDateTime.now();
    }


}

