package com.example.demo.entity;

import com.example.demo.common.enums.AccountType;
import com.example.demo.common.enums.Currency;
import com.example.demo.common.enums.TransferType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_from_iban", columnList = "fromIban"),
                @Index(name = "idx_transactions_to_iban", columnList = "toIban"),
                @Index(name = "idx_transactions_created_at", columnList = "createdAt"),
                @Index(name = "idx_transactions_amount", columnList = "amount")
        }
)
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
    private TransferType transferType;

    private String description;

    @PrePersist
    public void prePersist(){
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = LocalDateTime.now();
    }


}

