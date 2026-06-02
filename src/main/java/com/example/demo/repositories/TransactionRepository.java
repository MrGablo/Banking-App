package com.example.demo.repositories;

import com.example.demo.dtos.TransactionSearchRequest;
import com.example.demo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findAll(Pageable pageable);

    Page<Transaction> findByFromIbanOrToIban(String fromIban, String toIban, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromIban = ?1 AND t.createdAt BETWEEN ?2 AND ?3")
    Optional<BigDecimal> sumByFromIbanAndDate(String fromIban, LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT t
    FROM Transaction t
    WHERE
        (:#{#filter.startDate} IS NULL OR t.createdAt >= :#{#filter.startDate})
        AND (:#{#filter.endDate} IS NULL OR t.createdAt <= :#{#filter.endDate})
        AND (:#{#filter.minAmount} IS NULL OR t.amount >= :#{#filter.minAmount})
        AND (:#{#filter.maxAmount} IS NULL OR t.amount <= :#{#filter.maxAmount})
        AND (:#{#filter.exactAmount} IS NULL OR t.amount = :#{#filter.exactAmount})
        AND (
            :#{#filter.iban} IS NULL
            OR LOWER(t.fromIban) LIKE LOWER(CONCAT('%', :#{#filter.iban}, '%'))
            OR LOWER(t.toIban) LIKE LOWER(CONCAT('%', :#{#filter.iban}, '%'))
        )
""")
    Page<Transaction> searchTransactions(
            @Param("filter") TransactionSearchRequest filter,
            Pageable pageable
    );
}
