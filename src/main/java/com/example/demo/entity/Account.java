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
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 34, nullable = false, unique = true)
    private String iban;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private BigDecimal balance;

    private BigDecimal absoluteLimit;

    private BigDecimal dailyLimit;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();

    }
}
